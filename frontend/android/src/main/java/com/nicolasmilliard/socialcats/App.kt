package com.nicolasmilliard.socialcats

import com.crashlytics.android.Crashlytics
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.nicolasmilliard.socialcats.cloudmessaging.android.NotificationChannelHelper
import com.squareup.leakcanary.LeakCanary
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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

        Timber.plant(CrashlyticsTree(BuildConfig.DEBUG))

        Timber.i("App.onCreate()")
        val sessionManager = appComponent.sessionManager
        scope.launch(Dispatchers.Default) {
            sessionManager.sessions.collect {
                when {
                    it.authData?.user != null -> Crashlytics.setUserIdentifier(it.authData!!.user!!.id)
                    else -> Crashlytics.setUserIdentifier(null)
                }
            }
        }

        NotificationChannelHelper(this).initChannels()
        val cloudMessaging = appComponent.cloudMessaging
        Timber.d("Trigger cloudMessaging to start $cloudMessaging")
    }
}
