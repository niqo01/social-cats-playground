package com.nicolasmilliard.socialcats

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Build.VERSION_CODES.O
import android.os.Build.VERSION_CODES.Q
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.nicolasmilliard.socialcats.featureflags.FirebaseFeatureFlagProvider
import com.nicolasmilliard.socialcats.featureflags.MAX_PRIORITY
import com.nicolasmilliard.socialcats.featureflags.MEDIUM_PRIORITY
import com.nicolasmilliard.socialcats.featureflags.RuntimeBehavior.addProvider
import com.nicolasmilliard.socialcats.featureflags.RuntimeBehavior.clearFeatureFlagProviders
import com.nicolasmilliard.socialcats.featureflags.RuntimeBehavior.isFeatureEnabled
import com.nicolasmilliard.socialcats.featureflags.TEST_PRIORITY
import com.nicolasmilliard.socialcats.testsettings.RuntimeFeatureFlagProvider
import com.nicolasmilliard.socialcats.testsettings.TestFeatureFlagProvider
import com.nicolasmilliard.socialcats.testsettings.TestSetting
import leakcanary.LeakCanary
import timber.log.Timber

open class DebugApp : App() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        initializeFeatureFlag()
        LeakCanary.config.copy(dumpHeap = isFeatureEnabled(TestSetting.LEAK_CANARY))
        if (isFeatureEnabled(TestSetting.STRICT_MODE)) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
            val builder = VmPolicy.Builder()
                .detectActivityLeaks()
                .detectFileUriExposure()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
            if (SDK_INT >= M) {
                builder.detectCleartextNetwork()
            }
            if (SDK_INT >= O) {
                builder.detectContentUriWithoutPermission()
            }
            // Disable due to BottomNavigationView
            // https://github.com/material-components/material-components-android/issues/792
//            if (SDK_INT >= P) {
//                builder.detectNonSdkApiUsage()
//            }
            if (SDK_INT >= Q) {
                builder.detectCredentialProtectedWhileLocked()
                builder.detectImplicitDirectBoot()
            }
            StrictMode.setVmPolicy(builder.build())
        }
    }

    fun initializeFeatureFlag() {
        clearFeatureFlagProviders()
        val runtimeFeatureFlagProvider = RuntimeFeatureFlagProvider(MEDIUM_PRIORITY, this)

        if (runtimeFeatureFlagProvider.isFeatureEnabled(TestSetting.CRASH_APP)) {
            runtimeFeatureFlagProvider.setFeatureEnabled(TestSetting.CRASH_APP, false)
            Timber.e("Synthetic crash signal detected. Throwing in 3.. 2.. 1..")
            throw RuntimeException("Crash! Bang! Pow! This is only a test...")
        }

        addProvider(runtimeFeatureFlagProvider)
        addProvider(TestFeatureFlagProvider(TEST_PRIORITY))
        addProvider(
            FirebaseFeatureFlagProvider(
                MAX_PRIORITY,
                runtimeFeatureFlagProvider.isFeatureEnabled(TestSetting.DEBUG_FIREBASE)
            )
        )
    }
}
