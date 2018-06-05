package com.hd.screenrecordtool.view

import android.content.Intent
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.text.TextUtils
import com.hd.screencapture.help.Utils
import com.hd.screenrecordtool.R
import com.hd.screenrecordtool.help.ConfigHelp
import java.util.*


/**
 * Created by hd on 2018/5/31 .
 */
class ConfigFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

    private var help: ConfigHelp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.capture_config)
        help = ConfigHelp(activity)
        setContent(findPref(ConfigHelp.VIDEO_ENCODER), Utils.findAllVideoCodecName(), help!!.videoEncoder)
        setContent(findPref(ConfigHelp.VIDEO_BITRATE), resources.getStringArray(R.array.video_bitrates), help!!.videoBitrate)
        setContent(findPref(ConfigHelp.FPS), resources.getStringArray(R.array.video_framerates), help!!.fps)
        setContent(findPref(ConfigHelp.IFRAME_INTERVAL), resources.getStringArray(R.array.iframeintervals), help!!.iFrameInterval)
        setVideoProfile()
        setContent(findPref(ConfigHelp.AUDIO_ENCODER), Utils.findAllAudioCodecName(), help!!.audioEncoder)
        setContent(findPref(ConfigHelp.CHANNELS), resources.getStringArray(R.array.audio_channels), help!!.channels)
        setAudioPar()
        findPref<Preference>("capture").setOnPreferenceClickListener {
            startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
            false
        }
    }

    private fun setAudioPar() {
        val capabilities = Utils.findAudioCodecCapabilities(help!!.audioEncoder)

        val audioSampleRates = capabilities.audioCapabilities.supportedSampleRates
        val sampleRatesArray = arrayOfNulls<String>(audioSampleRates.size)
        for (i in audioSampleRates.indices) {
            sampleRatesArray[i] = audioSampleRates[i].toString()
        }
        setContent(findPref(ConfigHelp.SAMPLE_RATE), sampleRatesArray, help!!.sampleRate)

        //audio bitrate
        val audioBitrateRange = capabilities.audioCapabilities.bitrateRange
        val lower = Math.max(audioBitrateRange.lower / 1000, 80)
        val upper = audioBitrateRange.upper / 1000
        val rates = ArrayList<String>()
        var rate = lower
        while (rate < upper) {
            rates.add(rate.toString())
            rate += lower
        }
        rates.add(upper.toString())
        val bitrateArray = arrayOfNulls<String>(rates.size)
        setContent(findPref(ConfigHelp.AUDIO_BITRATE), rates.toTypedArray(), help!!.audioBitrate)

        //audio profile
        setContent(findPref(ConfigHelp.AAC_PROFILE), Utils.aacProfiles(), help!!.aacProfile)
    }

    private fun setVideoProfile() {
        val levels = ArrayList<String>()
        for (level in Utils.findVideoProfileLevel(help!!.videoEncoder)) {
            levels.add(Utils.avcProfileLevelToString(level))
        }
        val levelsArray = arrayOfNulls<String>(levels.size)
        setContent(findPref(ConfigHelp.AVC_PROFILE), levels.toTypedArray(), help!!.avcProfile)
    }

    private fun setContent(listPreference: ListPreference, entries: Array<String?>, content: Any) {
        var content = content
        listPreference.entries = entries
        listPreference.entryValues = entries
        if (content is String) {
            if (TextUtils.isEmpty(content)) {
                content = entries[0]!!
                listPreference.value = content
            }
        }
        listPreference.summary = content.toString()
        listPreference.onPreferenceChangeListener=this
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        try {
            preference?.summary = newValue as CharSequence
            return true
        } finally {
            if (ConfigHelp.VIDEO_ENCODER == preference?.key) {
                setVideoProfile()
            } else if (ConfigHelp.AUDIO_ENCODER == preference?.key) {
                setAudioPar()
            }
        }
    }

    private fun <T : Preference> findPref(key: CharSequence):T {
        return findPreference(key) as T
    }
}
