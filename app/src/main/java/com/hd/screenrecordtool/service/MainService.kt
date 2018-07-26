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
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.hd.screenrecordtool.R
import java.util.concurrent.atomic.AtomicBoolean

class MainService : Service(), View.OnTouchListener {

    interface ScreenRecordCallback {

        /** prepare record*/
        fun prepareRecord()

        /** start record*/
        fun startRecord()

        /** stop record*/
        fun stopRecord()

        /** stop service*/
        fun cancelRecord()
    }

    private lateinit var callback: ScreenRecordCallback

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

    private lateinit var recordLayout: ViewGroup

    private lateinit var windowManager: WindowManager

    private val record = AtomicBoolean(false)

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        val params = WindowManager.LayoutParams()
        windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= 26) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        params.format = PixelFormat.RGBA_8888
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.gravity = Gravity.START or Gravity.TOP
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        params.x = 0
        params.y = point.y - 500
        params.width = 335
        params.height = 120
        val inflater = LayoutInflater.from(application)
        recordLayout = inflater.inflate(R.layout.capture_button, null) as ViewGroup
        windowManager.addView(recordLayout, params)
        recordLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val tvHint = recordLayout.findViewById<TextView>(R.id.tvHint)
        recordLayout.findViewById<ImageButton>(R.id.btnControl).setOnClickListener { control -> reportState(control, tvHint) }
        recordLayout.findViewById<Button>(R.id.btnStopService).setOnClickListener { callback.cancelRecord() }
        recordLayout.setOnTouchListener(this)
    }

    fun prepare() {
        callback.prepareRecord()
    }

    private fun reportState(control: View, tvHint: TextView) {
        if (record.get()) {//stop
            record.set(false)
            control.background = resources.getDrawable(R.drawable.start_capture, null)
            tvHint.text = resources.getString(R.string.start)
            recordLayout.alpha = 1f
            callback.stopRecord()
        } else {//start
            record.set(true)
            control.background = resources.getDrawable(R.drawable.stop_capture, null)
            tvHint.text = resources.getString(R.string.stop)
            recordLayout.alpha = 0.05f
            callback.startRecord()
        }
    }

    override fun onDestroy() {
        windowManager.removeView(recordLayout)
        super.onDestroy()
    }

    private var preX: Float = 0.toFloat()

    private var preY:Float = 0.toFloat()

    private var x:Float = 0.toFloat()

    private var y:Float = 0.toFloat()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                preX = event.rawX
                preY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                x = event.rawX
                y = event.rawY
                val params = v?.layoutParams as WindowManager.LayoutParams
                params.x += (x - preX).toInt()
                params.y += (y - preY).toInt()
                windowManager.updateViewLayout(v, params)
                preX = x
                preY = y
                return true
            }
        }
        return false
    }
}
