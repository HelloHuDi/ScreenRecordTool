package com.hd.screenrecordtool.help

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.content.FileProvider
import java.io.File
import java.io.FileNotFoundException
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

    fun shareFile(context: Context, file: File) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val contentUri = FileProvider.getUriForFile(context,
                        context.applicationContext.packageName + ".FileProvider",file)
                intent.putExtra(Intent.EXTRA_STREAM,contentUri)
            } else {
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            }
            intent.type = "image/gif"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(intent, ""))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
}