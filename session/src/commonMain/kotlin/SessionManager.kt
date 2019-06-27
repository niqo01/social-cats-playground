package com.nicolasmilliard.socialcats.session

import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.AuthState.Authenticated
import com.nicolasmilliard.socialcats.auth.AuthState.UnAuthenticated
import com.nicolasmilliard.socialcats.session.SessionState.NoSession
import com.nicolasmilliard.socialcats.session.SessionState.Session
import com.nicolasmilliard.socialcats.store.SocialCatsStore
import com.nicolasmilliard.socialcats.store.User
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SessionManager(private val auth: Auth, private val store: SocialCatsStore) {

    private val _sessions = ConflatedBroadcastChannel<SessionState>()
    val sessions: Flow<SessionState> get() = _sessions.asFlow()

    suspend fun start() {
        coroutineScope {
            launch {
                auth.getAuthState()
                    .flatMapLatest { authState ->
                        when (authState) {
                            is UnAuthenticated -> flowOf(NoSession)
                            is Authenticated -> {
                                store.getCurrentUser(authState.authUser.uid)
                                    .map { Session(authState.authToken.token, it) }
                            }
                        }
                    }
                    .distinctUntilChanged()
                    .collect { _sessions.offer(it) }
            }
        }
    }
}

sealed class SessionState {
    object NoSession : SessionState()

    data class Session(
        val authToken: String,
        val user: User
    ) : SessionState()
}
