package com.nicolasmilliard.socialcats

import com.nicolasmilliard.socialcats.analytics.Analytics
import com.nicolasmilliard.socialcats.bugreporter.BugReporter
import com.nicolasmilliard.socialcats.featureflags.FirebaseFeatureFlagProvider
import com.nicolasmilliard.socialcats.featureflags.MAX_PRIORITY
import com.nicolasmilliard.socialcats.featureflags.MIN_PRIORITY
import com.nicolasmilliard.socialcats.featureflags.RuntimeBehavior
import com.nicolasmilliard.socialcats.featureflags.StoreFeatureFlagProvider
import com.nicolasmilliard.socialcats.session.SessionAuthState
import com.nicolasmilliard.socialcats.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

interface Initializer {
    fun initialize()
}

class AppInitializer(
    val appScope: CoroutineScope,
    val analytics: Analytics,
    val sessionManager: SessionManager,
    val bugReporter: BugReporter
) : Initializer {
    override fun initialize() {
        appScope.launch(Dispatchers.Default) {
            sessionManager.sessions.collect {
                when (it.authState) {
                    is SessionAuthState.Authenticated -> {
                        val uid = (it.authState as SessionAuthState.Authenticated).uId
                        analytics.setUserId(uid)
                        bugReporter.setUserId(uid)
                    }
                    else -> {
                        analytics.setUserId(null)
                        bugReporter.setUserId(null)
                    }
                }
            }
        }
        initializeFeatureFlag()
    }

    private fun initializeFeatureFlag() {
        RuntimeBehavior.addProvider(StoreFeatureFlagProvider(MIN_PRIORITY))
        RuntimeBehavior.addProvider(FirebaseFeatureFlagProvider(MAX_PRIORITY, false))
    }
}
