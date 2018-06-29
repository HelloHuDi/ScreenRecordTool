package com.hd.screenrecordtool.help

import android.graphics.Bitmap
import java.io.Serializable

/**
 * Created by hd on 2018/6/4 .
 */
data class VideoBean(var name: String? = "", var duration: String? = "00:00",
                     var size: String? = "0B", var bitmap: Bitmap? = null,
                     var filePath: String? = "", var overflowSize: Boolean = false) : Serializable
