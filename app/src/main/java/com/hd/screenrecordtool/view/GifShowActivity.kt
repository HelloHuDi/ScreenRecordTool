package com.hd.screenrecordtool.view

import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hd.screenrecordtool.R
import kotlinx.android.synthetic.main.activity_gif_show.*

class GifShowActivity : BaseActivity() {

    companion object {
        const val GIF_TAG = "screen_record_gif"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_show)
        val gifPath = intent.getStringExtra(GIF_TAG)
        val op = RequestOptions().placeholder(R.drawable.record).error(R.drawable.load_error)
        Glide.with(this).asGif().apply(op).load(gifPath).into(imageView)
        tvGifPath.text = gifPath
    }
}
