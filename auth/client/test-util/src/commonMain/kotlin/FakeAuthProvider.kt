package com.nicolasmilliard.socialcats.auth

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class FakeAuthProvider : AuthProvider {

    private val _authUserChannel: BroadcastChannel<AuthUser?> = ConflatedBroadcastChannel()
    private val authUserflow: Flow<AuthUser?> = _authUserChannel.asFlow()

    override fun getAuthUser(): Flow<AuthUser?> = authUserflow

    private val _authTokenChannel: BroadcastChannel<AuthToken?> = ConflatedBroadcastChannel()
    private val authTokenflow: Flow<AuthToken?> = _authTokenChannel.asFlow()

    override fun getAuthToken(): Flow<AuthToken?> = authTokenflow

    override suspend fun linkWithGoogleCredentials(googleIdToken: String) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun signInAnonymously() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun signInWithCredential(credential: AuthCredential) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    fun offerUser(user: AuthUser?) {
        _authUserChannel.offer(user)
    }

    fun offerToken(token: AuthToken?) {
        _authTokenChannel.offer(token)
    }
}

val anAuthToken = AuthToken("token")
val anAuthUser = AuthUser("uid", false, "name", "photo", "email", "phone")
val anAnonymousAuthUser = AuthUser("uid", true, null, null, null, null)
val anAuthenticatedState = AuthState.Authenticated(
    anAuthToken,
    anAuthUser
)
val anAnonymousAuthenticatedState = AuthState.Authenticated(
    anAuthToken,
    anAnonymousAuthUser
)
