package com.hd.screenrecordtool.presenter

import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.hd.screencapture.ScreenCapture
import com.hd.screencapture.callback.ScreenCaptureStreamCallback
import com.hd.screencapture.config.AudioConfig
import com.hd.screencapture.config.ScreenCaptureConfig
import com.hd.screencapture.config.VideoConfig
import com.hd.screencapture.help.Utils
import com.hd.screenrecordtool.BuildConfig
import com.hd.screenrecordtool.help.ConfigHelp
import com.hd.screenrecordtool.help.VideoHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hd on 2018/5/23 .
 */
class MainPresenter internal constructor(private val activity: AppCompatActivity) {

    private val screenCapture: ScreenCapture = ScreenCapture.with(activity)

    private var help: ConfigHelp = ConfigHelp(activity)

    val isCapturing: Boolean
        get() = screenCapture.isRunning

    private var captureConfig: ScreenCaptureConfig? = null

    fun startCapture() {
        if (!isCapturing) {
            initConfig()
            screenCapture.startCapture()
        } else {
            Toast.makeText(activity, "current is capturing state", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopCapture() {
        if (isCapturing) {
            screenCapture.stopCapture()
        } else {
            Toast.makeText(activity, "current is stopped state", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initConfig() {
        val videoConfig = if (help.useDefaultVideoConfig()) VideoConfig.initDefaultConfig(activity) else initVideoConfig(activity)
        var audioConfig: AudioConfig? = null
        if (help.hasAudio()) {
            audioConfig = if (help.useDefaultAudioConfig()) AudioConfig.initDefaultConfig() else initAudioConfig()
        }
        captureConfig = ScreenCaptureConfig.Builder()//
                .setAllowLog(BuildConfig.DEBUG)//
                .setRelevanceLifecycle(false)
                .setFile(setSelfFile()).setVideoConfig(videoConfig)//
                //if it is not set, then the voice will not be supported
                .setAudioConfig(audioConfig)//
                .setCaptureCallback(activity as ScreenCaptureStreamCallback)//
                .setAutoMoveTaskToBack(true)//
                .create()//
        Log.i("tag", "current using config ===>video config :" + videoConfig.toString()//
                + (if (audioConfig != null) "\n=====audio config :" + audioConfig.toString() else "") +
                "\n======ScreenCaptureConfig :" + captureConfig!!.toString())
        screenCapture.setConfig(captureConfig!!)
    }

    private fun initVideoConfig(activity: AppCompatActivity): VideoConfig {
        val videoConfig = VideoConfig(activity)
        videoConfig.bitrate = help.videoBitrate
        videoConfig.codecName = help.videoEncoder
        videoConfig.frameRate = help.fps
        videoConfig.iFrameInterval = help.iFrameInterval
        videoConfig.level = Utils.toProfileLevel(help.avcProfile)
        return videoConfig
    }

    private fun initAudioConfig(): AudioConfig {
        val audioConfig = AudioConfig()
        audioConfig.bitrate = help.audioBitrate
        audioConfig.channelCount = help.channels
        audioConfig.codecName = help.audioEncoder
        audioConfig.samplingRate = help.sampleRate
        audioConfig.level = Utils.toProfileLevel(help.aacProfile)
        return audioConfig
    }

    private fun setSelfFile(): File? {
        if (!VideoHelper.MAIN_FILE.exists() && !VideoHelper.MAIN_FILE.mkdir())
            return null
        return File(VideoHelper.MAIN_FILE, "screen_capture_" + SimpleDateFormat("yyyyMMdd-HH-mm-ss", Locale.US).format(Date()) + ".mp4")
    }
}
