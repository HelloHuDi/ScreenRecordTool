package com.hd.screenrecordtool.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ImageButton
import android.widget.TextView
import com.bumptech.glide.Glide
import com.hd.screenrecordtool.R
import com.hd.screenrecordtool.help.GifBean
import com.hd.screenrecordtool.help.GifHelper
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.activity_gif_list.*
import java.io.File

class GifListActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_list)
        initView()
        initGifLists()
    }

    private fun initView() {
        refresh.setColorSchemeResources(R.color.colorAccent)
        refresh.setOnRefreshListener {
            beanList = GifHelper.prepareBean(GifHelper.GIF_FILE)
            if (beanList.size == 0) {
                reportFileSizeState(0)
            } else {
                rvGif.adapter.notifyDataSetChanged()
            }
            refresh.isRefreshing = false
        }
    }

    private lateinit var beanList: ArrayList<GifBean>

    private fun initGifLists() {
        rvGif.layoutManager = GridLayoutManager(this, 2) as RecyclerView.LayoutManager?
        beanList = GifHelper.prepareBean(GifHelper.GIF_FILE)
        if (beanList.size == 0) reportFileSizeState(0)
        rvGif.adapter = object : CommonAdapter<GifBean>(this, R.layout.gif_item, beanList) {
            @SuppressLint("SetTextI18n")
            override fun convert(holder: ViewHolder?, t: GifBean?, position: Int) {
                if (holder != null && t != null) {
                    Glide.with(this@GifListActivity).asGif().load(t.filePath).into(holder.getView(R.id.ivGif))
                    val tvGifPath = holder.getView<TextView>(R.id.tvGifPath)
                    tvGifPath.text = t.name
                    tvGifPath.setTextIsSelectable(true)
                    tvGifPath.isSelected = true
                    holder.getView<TextView>(R.id.tvGifSize).text = " size : ${t.size}"
                    holder.getView<ImageButton>(R.id.btnDelete).setOnClickListener { reportAdapter(t, beanList, position) }
                    holder.getView<ImageButton>(R.id.btnShareGif).setOnClickListener { shareGif(t) }
                    holder.getView<ImageButton>(R.id.btnShowGif).setOnClickListener { showGif(t) }
                }
            }
        }
    }

    private fun CommonAdapter<GifBean>.reportAdapter(t: GifBean, beanList: ArrayList<GifBean>, position: Int) {
        GifHelper.deleteFile(t.filePath)
        beanList.remove(t)
        notifyItemRemoved(position)
        reportFileSizeState(1, false)
    }

    private fun shareGif(t: GifBean) {
        GifHelper.shareFile(this@GifListActivity, File(t.filePath))
    }

    private fun showGif(t: GifBean) {
        val intent = Intent(this@GifListActivity, GifShowActivity::class.java)
        intent.putExtra(GifShowActivity.GIF_TAG, t.filePath)
        startActivity(intent)
    }

    private fun reportFileSizeState(size: Int = 1, add: Boolean = size > 0) {
        Snackbar.make(refresh, if (size == 0) resources.getString(R.string.no_video_file) else
            String.format(resources.getString(if (add) R.string.add_video_file else R.string.delete_video_file),
                    Math.abs(size)), Snackbar.LENGTH_SHORT).show()
    }
}
