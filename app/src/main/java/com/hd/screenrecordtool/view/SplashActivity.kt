package com.hd.screenrecordtool.view

import android.Manifest
import android.content.Intent
import android.os.Bundle
import com.hd.screenrecordtool.R
import com.hd.splashscreen.text.SimpleConfig
import com.hd.splashscreen.text.SimpleSplashFinishCallback
import kotlinx.android.synthetic.main.activity_splash.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class SplashActivity : BaseActivity(), SimpleSplashFinishCallback, EasyPermissions.PermissionCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        if (hasPermission()) {
            goMain()
        } else {
            val simpleConfig = SimpleConfig(this)
            simpleConfig.callback = this
            screen.addConfig(simpleConfig)
            screen.start()
        }
    }

    override fun loadFinish() {
        if (!isDestroyed) {
            requestPermission()
        }
    }

    private val RESULT_CODE = 100
    private val writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE
    private val recordAudio = Manifest.permission.RECORD_AUDIO

    private fun requestPermission() {
        if (hasPermission()) {
            goMain()
        } else {
            EasyPermissions.requestPermissions(this, "You need to save the video to the local",
                    RESULT_CODE, writeExternalStorage, readExternalStorage, recordAudio)
        }
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (requestCode == RESULT_CODE) {
            requestPermission()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (hasPermission()) {
            goMain()
        }
    }

    private fun hasPermission() = EasyPermissions.hasPermissions(this, writeExternalStorage, readExternalStorage, recordAudio)
}
