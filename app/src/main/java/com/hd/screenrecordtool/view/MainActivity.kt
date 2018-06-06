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
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.hd.screenrecordtool.R
import com.hd.screenrecordtool.help.VideoBean
import com.hd.screenrecordtool.help.VideoHelper
import com.hd.screenrecordtool.presenter.MainPresenter
import com.hd.screenrecordtool.service.MainService
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import kotlin.concurrent.thread


@TargetApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity(), MainService.ScreenRecordCallback {

    private val TAG = MainActivity::class.java.simpleName

    private val mainPresenter: MainPresenter by lazy { MainPresenter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initCapture()
        initVideoList()
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

    override fun startRecord() {
        mainPresenter.startCapture()
    }

    override fun stopRecord() {
        mainPresenter.stopCapture()
    }

    override fun cancelRecord() {
        stopService(Intent(this@MainActivity, MainService::class.java))
    }

    fun setConfig(view: View) {
        startActivity(Intent(this, ConfigActivity::class.java))
    }

    internal inner class ConnectionService(private val callback: MainService.ScreenRecordCallback) : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {
            stopRecord()
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val mainService = (service as MainService.MainBinder).service
            mainService.addCallback(callback)
        }
    }

    private fun initVideoList() {
        rvVideo.layoutManager = GridLayoutManager(this, 2)
        val beanList = VideoHelper.prepareBean(VideoHelper.MAIN_FILE)
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
        thread { VideoHelper.formatBean(beanList) { runOnUiThread { rvVideo.adapter.notifyDataSetChanged() } } }
    }

    private fun CommonAdapter<VideoBean>.reportAdapter(t: VideoBean, beanList: ArrayList<VideoBean>, position: Int) {
        VideoHelper.deleteFile(t.filePath)
        beanList.remove(t)
        notifyItemRemoved(position)
    }

    @SuppressLint("ResourceAsColor")
    private fun transformGIF(t: VideoBean) {
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
            Snackbar.make(coordinator, resources.getString(R.string.transform_success), Snackbar.LENGTH_LONG).setAction(resources.getString(R.string.look_look), { seeSee(path) }).show()
        }, {
            //failed
            dialog?.dismiss()
            Snackbar.make(coordinator, resources.getString(R.string.transform_failed), Snackbar.LENGTH_SHORT).show()
        })
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
                val contentUri = FileProvider.getUriForFile(this, applicationContext.packageName + ".FileProvider", File(t.filePath))
                intent.setDataAndType(contentUri, "video/*")
            } else {
                val uri = Uri.parse(/*"file://" +*/ t.filePath)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setDataAndType(uri, "video/*")
            }
            startActivity(intent)
            Log.d(TAG, "use system player ：" + t.filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "use system player error ：$e")
            val intent = Intent(this@MainActivity, PlayActivity::class.java)
            intent.putExtra("video_path", t.filePath)
            startActivity(intent)
            Log.d(TAG, "use default player ：" + t.filePath)
        }
    }

    private fun initCapture() {
        fab.setOnClickListener { view ->
            if (Settings.canDrawOverlays(this@MainActivity)) {
                val intent = Intent(this@MainActivity, MainService::class.java)
                bindService(intent, ConnectionService(this), Context.BIND_AUTO_CREATE)
                moveTaskToBack(true)
            } else {
                Snackbar.make(view, resources.getString(R.string.need_permission), Snackbar.LENGTH_LONG)//
                        .setAction(resources.getString(R.string.to_set), { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) }).show()
            }
        }
    }
}

