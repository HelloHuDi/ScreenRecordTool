package com.hd.screenrecordtool.help

import android.media.MediaMetadataRetriever
import android.text.format.DateUtils
import java.io.File
import java.text.DecimalFormat

/**
 * Created by hd on 2018/6/4 .
 */
object VideoHelper {

    fun prepareBean(file: File): ArrayList<VideoBean> {
        val videoBeanArray = arrayListOf<VideoBean>()
        if (file.exists() && file.isDirectory && file.canRead()) {
            val fileList = file.list()
            for (path in fileList) {
                val videoBean = VideoBean()
                val childFile = File(file, path)
                videoBean.filePath = childFile.path
                videoBean.name = childFile.name
                videoBean.size = formatFileSize(childFile.length())
                videoBeanArray.add(videoBean)
            }
        }
        return videoBeanArray
    }

    inline fun formatBean(beans: ArrayList<VideoBean>, notify: () -> Unit) {
        val media = MediaMetadataRetriever()
        try {
            for (bean in beans) {
                media.setDataSource(bean.filePath)
                var duration = java.lang.Long.parseLong(media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
                duration /= 1000
                bean.duration = DateUtils.formatElapsedTime(duration)
                bean.bitmap = media.frameAtTime
            }
            notify()
        } finally {
            media.release()
        }
    }

    fun deleteFile(path: String?) {
        val file = File(path)
        if (file.exists() && file.isFile) {
            file.delete()
        }
    }

    fun transformGif(path: String?) {
        val file = File(path)
        //"库文件待完成"
    }

    private fun formatFileSize(fileS: Long): String {
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


}
