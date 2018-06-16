package com.hd.screenrecordtool.help

import android.graphics.BitmapFactory
import android.os.Environment
import java.io.File
import java.util.*


/**
 * Created by hd on 2018/6/15 .
 *
 */
object GifHelper : CaptureHelper() {

    val GIF_FILE = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "gif")

    fun prepareBean(file: File): ArrayList<GifBean> {
        val gifBeanArray = arrayListOf<GifBean>()
        if (file.exists() && file.isDirectory && file.canRead()) {
            val fileList = file.list()
            for (path in fileList) {
                val gifBean = GifBean()
                val childFile = File(file, path)
                if (childFile.isFile && file.canRead()) {
                    gifBean.filePath = childFile.path
                    gifBean.name = childFile.name
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(childFile.absolutePath, options)
                    val outHeight = options.outHeight
                    val outWidth = options.outWidth
                    gifBean.size = formatFileSize(childFile.length()) +" / "+outWidth+"x"+outHeight
                    gifBeanArray.add(gifBean)
                }
            }
        }
        return gifBeanArray
    }

}