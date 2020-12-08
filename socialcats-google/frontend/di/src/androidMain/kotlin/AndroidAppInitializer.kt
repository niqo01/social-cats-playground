package com.nicolasmilliard.socialcats.di

import android.content.Context
import com.nicolasmilliard.socialcats.cloudmessaging.CloudMessaging
import com.nicolasmilliard.socialcats.cloudmessaging.NotificationChannelHelper
import com.nicolasmilliard.socialcats.featureflags.FirebaseFeatureFlagProvider
import com.nicolasmilliard.socialcats.featureflags.MAX_PRIORITY
import com.nicolasmilliard.socialcats.featureflags.MIN_PRIORITY
import com.nicolasmilliard.socialcats.featureflags.RuntimeBehavior
import com.nicolasmilliard.socialcats.featureflags.StoreFeatureFlagProvider
import timber.log.Timber

class AndroidAppInitializer(val context: Context, val cloudMessaging: CloudMessaging) : PlatformInitializer {

    override fun initialize() {
        NotificationChannelHelper(context).initChannels()
        Timber.d("Trigger cloudMessaging to start $cloudMessaging")
    }

    override fun initializeFeatureFlag() {
        RuntimeBehavior.addProvider(StoreFeatureFlagProvider(MIN_PRIORITY))
        RuntimeBehavior.addProvider(FirebaseFeatureFlagProvider(MAX_PRIORITY, false))
    }
}
