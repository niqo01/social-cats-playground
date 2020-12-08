package com.nicolasmilliard.socialcats.di

import com.nicolasmilliard.socialcats.analytics.Analytics
import com.nicolasmilliard.socialcats.bugreporter.BugReporter
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
    val bugReporter: BugReporter,
    val platformInitializer: PlatformInitializer
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
        platformInitializer.initialize()
        platformInitializer.initializeFeatureFlag()
    }
}

interface PlatformInitializer : Initializer {
    fun initializeFeatureFlag()
}
