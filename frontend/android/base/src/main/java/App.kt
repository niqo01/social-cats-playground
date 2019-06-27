package com.nicolasmilliard.socialcats

import androidx.work.Configuration
import com.crashlytics.android.Crashlytics
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.nicolasmilliard.socialcats.cloudmessaging.CloudMessagingProvider
import com.nicolasmilliard.socialcats.cloudmessaging.NotificationChannelHelper
import com.nicolasmilliard.socialcats.featureflags.FirebaseFeatureFlagProvider
import com.nicolasmilliard.socialcats.featureflags.MAX_PRIORITY
import com.nicolasmilliard.socialcats.featureflags.MIN_PRIORITY
import com.nicolasmilliard.socialcats.featureflags.RuntimeBehavior
import com.nicolasmilliard.socialcats.featureflags.StoreFeatureFlagProvider
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

        Timber.plant(CrashlyticsTree())

        Timber.i("App.onCreate()")
        scope.launch(Dispatchers.Default) {
            sessionManager.sessions.collect {
                when {
                    it.authData?.user != null -> Crashlytics.setUserIdentifier(it.authData!!.user!!.id)
                    else -> Crashlytics.setUserIdentifier(null)
                }
            }
        }
        initializeFeatureFlag()

        NotificationChannelHelper(this).initChannels()
        Timber.d("Trigger cloudMessaging to start $cloudMessaging")
    }

    override val appComponent by lazy { AppComponent(this, scope) }
    override val store by lazy { appComponent.store }
    override val sessionManager by lazy { appComponent.sessionManager }
    override val cloudMessaging by lazy { appComponent.cloudMessaging }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    open fun initializeFeatureFlag() {
        RuntimeBehavior.addProvider(StoreFeatureFlagProvider(MIN_PRIORITY))
        RuntimeBehavior.addProvider(FirebaseFeatureFlagProvider(MAX_PRIORITY, false))
    }
}
