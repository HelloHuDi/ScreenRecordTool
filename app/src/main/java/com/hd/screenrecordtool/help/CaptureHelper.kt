package com.hd.screenrecordtool.help

import java.io.File
import java.text.DecimalFormat


/**
 * Created by hd on 2018/6/15 .
 *
 */
open class CaptureHelper {

    fun deleteFile(path: String?) {
        val file = File(path)
        if (file.exists() && file.isFile) {
            file.delete()
        }
    }

   protected fun formatFileSize(fileS: Long): String {
        val df = DecimalFormat("#.00")
        val fileSizeString: String
        val wrongSize = "0B"
        if (fileS == 0L) {
            return wrongSize
        }
        fileSizeString = when {
            fileS < 1024 -> df.format(fileS.toDouble()) + "B"
            fileS < 1048576 -> df.format(fileS.toDouble() / 1024) + "K"
            fileS < 1073741824 -> df.format(fileS.toDouble() / 1048576) + "M"
            else -> df.format(fileS.toDouble() / 1073741824) + "G"
        }
        return fileSizeString
    }

    /** 25M*/
    private val maxOverflowSize = 25*1048576

    protected fun formatOverflow(length: Long): Boolean {
        return length>maxOverflowSize
    }
}