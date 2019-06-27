package com.nicolasmilliard.socialcats.cloudmessaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import com.nicolasmilliard.socialcats.cloudmessaging.android.R

class NotificationChannelHelper(context: Context) : ContextWrapper(context) {

    private val manager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun initChannels() {
        if (SDK_INT < O) return
        val chan1 = NotificationChannel(Channels.GENERAL,
            getString(R.string.notification_channel_title_general), NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(chan1)
    }
}
