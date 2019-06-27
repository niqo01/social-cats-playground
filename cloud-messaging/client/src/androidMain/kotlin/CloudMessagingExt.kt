package com.nicolasmilliard.socialcats.cloudmessaging

import android.content.Context

interface CloudMessagingProvider {
    val cloudMessaging: CloudMessaging
}

val Context.cloudMessaging get() = (applicationContext as CloudMessagingProvider).cloudMessaging
