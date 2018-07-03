package com.hd.screenrecordtool.help

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.text.format.DateUtils
import com.hd.transfer.TransferGif
import java.io.File
import java.util.*
import kotlin.concurrent.thread


/**
 * Created by hd on 2018/6/4 .
 */
object VideoHelper : CaptureHelper() {

    private const val endName=".mp4"

    fun prepareBean(file: File): ArrayList<VideoBean> {
        val videoBeanArray = arrayListOf<VideoBean>()
        if (file.exists() && file.isDirectory && file.canRead()) {
            val fileList = file.list()
            for (path in fileList) {
                val videoBean = VideoBean()
                val childFile = File(file, path)
                if (childFile.isFile && file.canRead() && path.endsWith(endName)) {
                    videoBean.filePath = childFile.path
                    videoBean.name = childFile.name
                    videoBean.size = formatFileSize(childFile.length())
                    videoBean.overflowSize = formatOverflow(childFile.length())
                    videoBeanArray.add(videoBean)
                }
            }
        }
        return videoBeanArray
    }

    inline fun formatBean(beans: ArrayList<VideoBean>, notify: () -> Unit) {
        val media = MediaMetadataRetriever()
        try {
            for (bean in beans) {
                try {
                    media.setDataSource(bean.filePath)
                    var duration = java.lang.Long.parseLong(media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
                    duration /= 1000
                    bean.duration = DateUtils.formatElapsedTime(duration)
                    bean.bitmap = media.frameAtTime
                } catch (e: Exception) {
                    continue
                }
            }
            notify()
        } finally {
            media.release()
        }
    }

    inline fun formatBean(file: File, beans: ArrayList<VideoBean>, notifySize: (beans: ArrayList<VideoBean>) -> Unit,
                          crossinline notifyBitmap: (beans: ArrayList<VideoBean>) -> Unit) {
        val beanList = prepareBean(file)
        if (beanList.size == beans.size) {
            var same = false
            for (newBean in beanList) {
                same = false
                for (oldBean in beanList) {
                    same = (oldBean.name == newBean.name) && (oldBean.filePath == newBean.filePath)
                            && (oldBean.size == newBean.size)
                    if (same) break
                }
                if (!same) break
            }
            if (same) {
                notifySize(beans)
            } else {
                notifySize(beanList)
                formatBean(beanList) { notifyBitmap(beanList) }
            }
        } else {
            notifySize(beanList)
            formatBean(beanList) { notifyBitmap(beanList) }
        }
    }

    @SuppressLint("MissingPermission")
    inline fun transformGif(path: String?, running: () -> Unit, crossinline success: (path: String) -> Unit, crossinline failed: () -> Unit) {
        running()
        var file = File(path)
        val gifFileName = file.name.split(".")[0].trim() + ".gif"
        thread {
            file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "gif")
            if (!file.exists() && !file.mkdir()) {
                failed()
            } else {
                //val palettePath = File(file, "palette.jpeg").absolutePath
                val outputGifPath = File(file, gifFileName).absolutePath
                val completed = TransferGif.transfer(path!!, outputGifPath)
                if (completed) {
                    success(outputGifPath)
                } else {
                    failed()
                }
            }
        }
    }

}
