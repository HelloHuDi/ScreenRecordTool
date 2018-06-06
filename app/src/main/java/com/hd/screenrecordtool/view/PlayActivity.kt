package com.hd.screenrecordtool.view

import android.os.Bundle
import com.hd.screenrecordtool.R
import kotlinx.android.synthetic.main.activity_play.*

class PlayActivity : BaseActivity() {

    companion object {
        const val PLAY_PATH="video_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        try {
            val path = intent.getStringExtra(PLAY_PATH)
            videoView.setLoopPlay(false)
            videoView.setPlayPath(path)
            videoView.start()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    override fun onStop() {
        try {
            videoView.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onStop()
    }
}
