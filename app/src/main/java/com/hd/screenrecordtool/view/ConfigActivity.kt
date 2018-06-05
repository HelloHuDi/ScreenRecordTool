package com.hd.screenrecordtool.view

import android.os.Bundle

class ConfigActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(android.R.id.content, ConfigFragment()).commit()
    }
}
