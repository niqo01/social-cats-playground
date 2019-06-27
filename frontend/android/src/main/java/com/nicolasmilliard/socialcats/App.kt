package com.nicolasmilliard.socialcats

import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.squareup.leakcanary.LeakCanary
import timber.log.Timber

class App : SplitCompatApplication(), AppComponentProvider {

    override val appComponent = AppComponent(this)

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
    }
}
