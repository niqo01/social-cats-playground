package com.nicolasmilliard.socialcats

import android.content.Context
import androidx.work.Configuration
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.nicolasmilliard.socialcats.base.BuildConfig
import com.nicolasmilliard.socialcats.bugreporter.BugReporter
import com.nicolasmilliard.socialcats.di.AppInitializer
import com.nicolasmilliard.socialcats.di.initKoin
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import org.koin.dsl.module
import timber.log.Timber

open class App : SplitCompatApplication(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            modules(
                module {
                    single<Context> { this@App }
                    single(named("appName")) { "SocialCats-${BuildConfig.BUILD_TYPE}" }
                }
            )
        }

        Timber.plant(BugReporterTree(bugReporter))
        Timber.i("App.onCreate()")

        appInitializer.initialize()
    }

    val appInitializer: AppInitializer by inject()
    val bugReporter: BugReporter by inject()

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
