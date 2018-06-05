package com.hd.screenrecordtool.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import com.hd.screenrecordtool.R

class MainService : Service() {

   lateinit var toucherLayout: ViewGroup
   lateinit var params: WindowManager.LayoutParams
   lateinit var windowManager: WindowManager

    internal var ibCapture: Button? = null

    //状态栏高度.
    internal var statusBarHeight = -1

    private var callback: ScreenRecordCallback? = null

    interface ScreenRecordCallback {

        fun startRecord()

        fun stopRecord()

        fun cancelRecord()
    }

    fun addCallback(callback: ScreenRecordCallback) {
        this.callback = callback
    }

    override fun onBind(intent: Intent): IBinder? {
        return MainBinder()
    }

    inner class MainBinder : Binder() {

        val service: MainService
            get() = this@MainService
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "MainService Created")
        createToucher()
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun createToucher() {
        //赋值WindowManager&LayoutParam.
        params = WindowManager.LayoutParams()
        windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= 26) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        //设置窗口初始停靠位置.
        params.gravity = Gravity.START or Gravity.TOP
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        Log.d("tag", "===屏幕==" + point.x + "++++" + point.y)
        params.x = 0
        params.y = point.y - 500


        //设置悬浮窗口长宽数据.
        params.width = 120
        params.height = 120

        val inflater = LayoutInflater.from(application)
        //获取浮动窗口视图所在布局.
        toucherLayout = inflater.inflate(R.layout.capture_button, null) as ViewGroup
        //添加toucherlayout
        windowManager.addView(toucherLayout, params)

        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        //用于检测状态栏高度.
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        Log.i(TAG, "状态栏高度为:$statusBarHeight")

        //浮动窗口按钮.
        ibCapture = toucherLayout.findViewById(R.id.ibCapture)

        ibCapture!!.setOnClickListener(object : View.OnClickListener {
            internal var hints = LongArray(2)

            override fun onClick(v: View) {
                Log.i(TAG, "点击了")
                System.arraycopy(hints, 1, hints, 0, hints.size - 1)
                hints[hints.size - 1] = SystemClock.uptimeMillis()
                if (SystemClock.uptimeMillis() - hints[0] >= 700) {
                    Log.i(TAG, "要执行")
                    Toast.makeText(this@MainService, "连续点击两次以退出", Toast.LENGTH_SHORT).show()
                } else {
                    Log.i(TAG, "即将关闭")
                    stopSelf()
                }
            }
        })

        ibCapture!!.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    params.x = event.rawX.toInt()
                    params.y = event.rawY.toInt()
                    windowManager.updateViewLayout(toucherLayout, params)
                }
            }
            false
        }

    }

    override fun onDestroy() {
        if (ibCapture != null) {
            windowManager.removeView(toucherLayout)
        }
        super.onDestroy()
    }

    companion object {

        private val TAG = "MainService"
    }
}
