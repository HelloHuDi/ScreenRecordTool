package com.hd.screenrecordtool.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hd.screenrecordtool.R
import kotlinx.android.synthetic.main.activity_gif_show.*

class GifShowActivity : AppCompatActivity() {

    companion object {
        const val GIF_TAG="screen_record_gif"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_show)
        Glide.with(this).asGif().load(intent.getStringExtra(GIF_TAG)).into(imageView)
    }
}
