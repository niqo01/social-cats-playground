package com.nicolasmilliard.socialcats.session

import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.AuthState.Authenticated
import com.nicolasmilliard.socialcats.auth.AuthState.UnAuthenticated
import com.nicolasmilliard.socialcats.session.SessionState.NoSession
import com.nicolasmilliard.socialcats.session.SessionState.Session
import com.nicolasmilliard.socialcats.store.SocialCatsStore
import com.nicolasmilliard.socialcats.store.User
import com.nicolasmilliard.socialcats.test.IO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SessionManager(private val auth: Auth, private val store: SocialCatsStore) {

    private val _sessions = ConflatedBroadcastChannel<SessionState>()
    val sessions: Flow<SessionState> get() = _sessions.asFlow()
    /**
     * Get auth and current user info if any
     * Get device information
     */
    suspend fun start() = withContext(Dispatchers.IO()) {
        launch {
            auth.getAuthState()
                .flatMapLatest { authState ->
                    when (authState) {
                        is UnAuthenticated -> flowOf(NoSession)
                        is Authenticated -> {
                            store.getCurrentUser(authState.authUser.uid)
                                .map { Session(authState.authToken.token, authState.authUser.isAnonymous, it) }
                        }
                    }
                }
                .distinctUntilChanged()
                .collect { _sessions.offer(it) }
        }
    }
}

sealed class SessionState {
    object NoSession : SessionState()

    data class Session(
        val authToken: String,
        val isAnonymous: Boolean,
        val user: User
    ) : SessionState()
}
