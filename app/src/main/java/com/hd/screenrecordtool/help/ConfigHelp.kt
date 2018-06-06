package com.hd.screenrecordtool.help

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by hd on 2018/5/31 .
 */
class ConfigHelp(context: Context) {

    private val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val videoEncoder: String
        get() = sp.getString(VIDEO_ENCODER, "")

    val videoBitrate: Int
        get() = Integer.parseInt(sp.getString(VIDEO_BITRATE, "12000000"))

    val fps: Int
        get() = Integer.parseInt(sp.getString(FPS, "60"))

    val iFrameInterval: Int
        get() = Integer.parseInt(sp.getString(IFRAME_INTERVAL, "1"))

    val avcProfile: String
        get() = sp.getString(AVC_PROFILE, "")

    val audioEncoder: String
        get() = sp.getString(AUDIO_ENCODER, "")

    val channels: Int
        get() = Integer.parseInt(sp.getString(CHANNELS, "1"))

    val sampleRate: Int
        get() = Integer.parseInt(sp.getString(SAMPLE_RATE, "44100"))

    val audioBitrate: Int
        get() = Integer.parseInt(sp.getString(AUDIO_BITRATE, "2"))

    val aacProfile: String
        get() = sp.getString(AAC_PROFILE, "")

    fun useDefaultAudioConfig(): Boolean {
        return sp.getBoolean(DEFAULT_VIDEO_CONFIG, false)
    }

    fun useDefaultVideoConfig(): Boolean {
        return sp.getBoolean(DEFAULT_AUDIO_CONFIG, true)
    }

    fun hasAudio(): Boolean {
        return sp.getBoolean(HAS_AUDIO, false)
    }

    companion object {

        const val HAS_AUDIO = "has_audio"
        const val VIDEO_ENCODER = "video_encoder"
        const val VIDEO_BITRATE = "video_bitrate"
        const val FPS = "fps"
        const val IFRAME_INTERVAL = "iFrame_interval"
        const val AVC_PROFILE = "avc_profile"
        const val AUDIO_ENCODER = "audio_encoder"
        const val CHANNELS = "channels"
        const val SAMPLE_RATE = "sample_rate"
        const val AUDIO_BITRATE = "audio_bitrate"
        const val AAC_PROFILE = "aac_profile"
        const val VIDEO_FILE = "video_file"
        const val DEFAULT_VIDEO_CONFIG = "defaultVideoConfig"
        const val DEFAULT_AUDIO_CONFIG = "defaultAudioConfig"
    }
}
