package com.nicolasmilliard.socialcats

import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.nicolasmilliard.socialcats.cloudmessaging.NotificationChannelHelper
import com.squareup.leakcanary.LeakCanary
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus
import timber.log.Timber

class App : SplitCompatApplication(), AppComponentProvider {

    private val scope = MainScope() + CoroutineName("App")
    override val appComponent = AppComponent(this, scope)

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.i("App.onCreate()")

        NotificationChannelHelper(this).initChannels()
        val cloudMessaging = appComponent.cloudMessaging
        Timber.d("Trigger cloudMessaging to start $cloudMessaging")
    }
}
