package com.nicolasmilliard.socialcats.ui

import android.app.Activity
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.provider.Settings

class CheckConnectivityHandler(private val activity: Activity, private val requestCode: Int) {

    operator fun invoke() {
        if (SDK_INT >= Q) {
            val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            activity.startActivityForResult(panelIntent, requestCode)
        }
    }
}
