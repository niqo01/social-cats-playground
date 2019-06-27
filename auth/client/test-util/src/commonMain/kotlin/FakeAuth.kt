package com.nicolasmilliard.socialcats.auth

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class FakeAuth : Auth {
    private val authChannel: BroadcastChannel<AuthState> = ConflatedBroadcastChannel()
    private val flow: Flow<AuthState> = authChannel.asFlow()

    override fun getAuthState(): Flow<AuthState> {
        return flow
    }

    fun offer(authState: AuthState) {
        authChannel.offer(authState)
    }
}

val aToken = AuthToken("token")
val aAuthUser = AuthUser("uid", "name", "photo", "email", "phone")
