package com.nicolasmilliard.socialcats

import androidx.work.Configuration
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.nicolasmilliard.socialcats.cloudmessaging.CloudMessagingProvider
import com.nicolasmilliard.socialcats.cloudmessaging.NotificationChannelHelper
import com.nicolasmilliard.socialcats.featureflags.FirebaseFeatureFlagProvider
import com.nicolasmilliard.socialcats.featureflags.MAX_PRIORITY
import com.nicolasmilliard.socialcats.featureflags.MIN_PRIORITY
import com.nicolasmilliard.socialcats.featureflags.RuntimeBehavior
import com.nicolasmilliard.socialcats.featureflags.StoreFeatureFlagProvider
import com.nicolasmilliard.socialcats.session.SessionAuthState
import com.nicolasmilliard.socialcats.session.SessionProvider
import com.nicolasmilliard.socialcats.store.StoreProvider
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber

open class App : SplitCompatApplication(), AppComponentProvider, StoreProvider, SessionProvider, CloudMessagingProvider,
    Configuration.Provider {

    private val scope = MainScope() + CoroutineName("App")

    override fun onCreate() {
        super.onCreate()

        Timber.plant(BugReporterTree(bugReporter))
        Timber.i("App.onCreate()")

        appInitializer.initialize()

        NotificationChannelHelper(this).initChannels()
        Timber.d("Trigger cloudMessaging to start $cloudMessaging")
    }

    val appInitializer by lazy { appComponent.appInitializer }
    override val appComponent by lazy { AppComponent(this, scope) }
    override val store by lazy { appComponent.store }
    override val sessionManager by lazy { appComponent.sessionManager }
    val bugReporter by lazy { appComponent.bugReporter }
    override val cloudMessaging by lazy { appComponent.cloudMessaging }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

}
