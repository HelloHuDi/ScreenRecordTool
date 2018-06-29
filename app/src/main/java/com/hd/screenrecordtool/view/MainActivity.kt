package com.hd.screenrecordtool.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.hd.screencapture.callback.ScreenCaptureCallback
import com.hd.screencapture.help.ScreenCaptureState
import com.hd.screenrecordtool.R
import com.hd.screenrecordtool.help.ConfigHelp
import com.hd.screenrecordtool.help.VideoBean
import com.hd.screenrecordtool.help.VideoHelper
import com.hd.screenrecordtool.presenter.MainPresenter
import com.hd.screenrecordtool.service.MainService
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread


@TargetApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity(), MainService.ScreenRecordCallback, ScreenCaptureCallback {

    private val TAG = MainActivity::class.java.simpleName

    private val mainPresenter: MainPresenter by lazy { MainPresenter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initView()
        initVideoList()
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (mainPresenter.isCapturing) {
            AlertDialog.Builder(this)//
                    .setMessage("Screen currently is recording! Confirm the stop?")//
                    .setCancelable(false)//
                    .setPositiveButton(android.R.string.ok) { _, _ -> super.onBackPressed() }//
                    .setNegativeButton(android.R.string.cancel, { _, _ -> moveTaskToBack(true) })//
                    .create()//
                    .show()
        } else {
            super.onBackPressed()
        }
    }

    override fun captureState(state: ScreenCaptureState?) {
        Log.d(TAG, "record state ：$state")
        runOnUiThread {
            val status = when (state) {
                ScreenCaptureState.COMPLETED -> {
                    refreshDataAgain()
                    resources.getString(R.string.complete)
                }
                ScreenCaptureState.FAILED -> resources.getString(R.string.failed)
                else -> return@runOnUiThread
            }
            Toast.makeText(this, String.format(resources.getString(R.string.record_video), status), Toast.LENGTH_SHORT).show()
        }
    }

    override fun captureTime(time: Long) {
        Log.d(TAG, "record time ：$time+${DateUtils.formatElapsedTime(time)}")
    }

    override fun prepareRecord() {
        moveTaskToBack(true)
    }

    override fun startRecord() {
        mainPresenter.startCapture()
    }

    override fun stopRecord() {
        mainPresenter.stopCapture()
    }

    override fun cancelRecord() {
        stopService()
    }

    fun setConfig(view: View) {
        startActivity(Intent(this, ConfigActivity::class.java))
    }

    fun showGifLists(view: View) {
        startActivity(Intent(this, GifListActivity::class.java))
    }

    private var serviceBind = false

    internal inner class ConnectionService(private val callback: MainService.ScreenRecordCallback) : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBind = false
            stopRecord()
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            serviceBind = true
            val mainService = (service as MainService.MainBinder).service
            mainService.addCallback(callback)
            mainService.prepare()
        }
    }

    private val loadCompleted = AtomicBoolean(false)

    private lateinit var beanList: ArrayList<VideoBean>

    private fun initVideoList() {
        loadCompleted.set(false)
        rvVideo.layoutManager = GridLayoutManager(this, 2) as RecyclerView.LayoutManager?
        beanList = VideoHelper.prepareBean(File(ConfigHelp(this@MainActivity).saveVideoPath))
        if (beanList.size == 0) reportFileSizeState(0)
        rvVideo.adapter = object : CommonAdapter<VideoBean>(this, R.layout.video_item, beanList) {
            override fun convert(holder: ViewHolder?, t: VideoBean?, position: Int) {
                if (holder != null && t != null) {
                    val ivBgFrame = holder.getView<ImageView>(R.id.ivBgFrame)
                    val ivFrame = holder.getView<ImageView>(R.id.ivFrame)
                    if (t.bitmap != null) {
                        ivBgFrame.visibility = View.GONE
                        ivFrame.visibility = View.VISIBLE
                        Glide.with(this@MainActivity).asBitmap().load(t.bitmap).into(ivFrame)
                    } else {
                        ivBgFrame.visibility = View.VISIBLE
                        ivFrame.visibility = View.GONE
                    }
                    holder.getView<TextView>(R.id.tvDuration).text = t.duration
                    holder.getView<TextView>(R.id.tvSize).text = t.size
                    holder.getView<ImageButton>(R.id.btnDelete).setOnClickListener { reportAdapter(t, beanList, position) }
                    holder.getView<ImageButton>(R.id.btnTransform).setOnClickListener { transformGIF(t) }
                    holder.getView<ImageButton>(R.id.btnPlay).setOnClickListener { playVideo(t) }
                }
            }
        }
        thread {
            VideoHelper.formatBean(beanList) { runOnUiThread { refreshAdapter() } }
        }
    }

    private fun refreshAdapter(needRefresh: Boolean = true) {
        if (needRefresh) rvVideo.adapter.notifyDataSetChanged()
        refresh.isRefreshing = false
        loadCompleted.set(true)
    }

    private fun CommonAdapter<VideoBean>.reportAdapter(t: VideoBean, beanList: ArrayList<VideoBean>, position: Int) {
        if (loadCompleted.get()) {
            VideoHelper.deleteFile(t.filePath)
            beanList.remove(t)
            notifyItemRemoved(position)
            reportFileSizeState(1, false)
        }
    }

    private fun transformGIF(t: VideoBean) {
        if (t.overflowSize) {
            Snackbar.make(coordinator, resources.getString(R.string.transform_hide), Snackbar.LENGTH_LONG)
                    .setAction(resources.getString(R.string.transform_continue), { transfer(t) }).show()
        } else {
            transfer(t)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun transfer(t: VideoBean) {
        var dialog: MaterialDialog? = null
        VideoHelper.transformGif(t.filePath, {
            //transforming
            dialog = MaterialDialog.Builder(this)
                    .backgroundColor(android.R.color.darker_gray)
                    .customView(R.layout.dialog, true)
                    .cancelable(false)
                    .show()
        }, { path ->
            //success
            dialog?.dismiss()
            notifyRefresh(path)
            Snackbar.make(coordinator, resources.getString(R.string.transform_success), Snackbar.LENGTH_LONG)
                    .setAction(resources.getString(R.string.look_look), { seeSee(path) }).show()
        }, {
            //failed
            dialog?.dismiss()
            Snackbar.make(coordinator, resources.getString(R.string.transform_failed), Snackbar.LENGTH_SHORT).show()
        })
    }

    private fun notifyRefresh(path: String) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                .addCategory(Intent.CATEGORY_DEFAULT).setData(Uri.parse(path))
        sendBroadcast(intent)
    }

    private fun seeSee(path: String) {
        val intent = Intent(this@MainActivity, GifShowActivity::class.java)
        intent.putExtra(GifShowActivity.GIF_TAG, path)
        startActivity(intent)
    }

    private fun playVideo(t: VideoBean) {
        try {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                val contentUri = FileProvider.getUriForFile(this,
                        applicationContext.packageName + ".FileProvider", File(t.filePath))
                intent.setDataAndType(contentUri, "video/*")
            } else {
                val uri = Uri.parse(/*"file://" +*/ t.filePath)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setDataAndType(uri, "video/*")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent(this@MainActivity, PlayActivity::class.java)
            intent.putExtra(PlayActivity.PLAY_PATH, t.filePath)
            startActivity(intent)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun initView() {
        refresh.setColorSchemeResources(R.color.colorAccent)
        refresh.setOnRefreshListener {
            if (loadCompleted.get()) {
                refreshDataAgain()
            } else {
                refresh.isRefreshing = false
            }
        }
        fab.setOnClickListener { view ->
            if (Settings.canDrawOverlays(this@MainActivity)) {
                startService()
            } else {
                Snackbar.make(view, resources.getString(R.string.need_permission), Snackbar.LENGTH_LONG)//
                        .setAction(resources.getString(R.string.to_set), {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName"))
                            startActivity(intent)
                        }).show()
            }
        }
    }

    private fun refreshDataAgain() {
        loadCompleted.set(false)
        thread {
            VideoHelper.formatBean(File(ConfigHelp(this@MainActivity).saveVideoPath), beanList, { beans ->
                runOnUiThread {
                    if (beanList != beans) {
                        val changeSize = beans.size - beanList.size
                        reportFileSizeState(changeSize)
                        this@MainActivity.beanList.clear()
                        this@MainActivity.beanList.addAll(beans)
                        refreshAdapter()
                    } else {
                        refreshAdapter(false)
                    }
                }
            }, { beans ->
                runOnUiThread {
                    this@MainActivity.beanList.clear()
                    this@MainActivity.beanList.addAll(beans)
                    refreshAdapter()
                }
            })
        }
    }

    private fun reportFileSizeState(size: Int = 1, add: Boolean = size > 0) {
        Snackbar.make(coordinator, if (size == 0) resources.getString(R.string.no_video_file) else
            String.format(resources.getString(if (add) R.string.add_video_file else R.string.delete_video_file),
                    Math.abs(size)), Snackbar.LENGTH_SHORT).show()
    }

    private val connectionService: ConnectionService by lazy { ConnectionService(this) }

    private fun startService() {
        val intent = Intent(this@MainActivity, MainService::class.java)
        bindService(intent, connectionService, Context.BIND_AUTO_CREATE)
    }

    private fun stopService() {
        if (serviceBind) {
            unbindService(connectionService)
            Toast.makeText(this, resources.getString(R.string.sign_out_record), Toast.LENGTH_SHORT).show()
        }
    }

}

