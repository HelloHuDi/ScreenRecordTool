package com.hd.screenrecordtool.view

import android.os.Bundle
import com.hd.screenrecordtool.R
import kotlinx.android.synthetic.main.activity_play.*

class PlayActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        try {
            val path = intent.getStringExtra("video_path")
            videoView.setLoopPlay(false)
            videoView.setPlayPath(path)
            videoView.start()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            videoView.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
