package com.nicolasmilliard.socialcats.session

import android.content.Context

interface SessionProvider {
    val sessionManager: SessionManager
}

val Context.sessionManager get() = (applicationContext as SessionProvider).sessionManager
