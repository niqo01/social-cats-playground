package com.nicolasmilliard.socialcats.auth

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FakeAuthProvider : AuthProvider {

    private val _authUserChannel = MutableStateFlow<AuthUser?>(null)
    private val authUserflow: StateFlow<AuthUser?> = _authUserChannel

    override fun getAuthUser(): Flow<AuthUser?> = authUserflow

    private val _authTokenChannel = MutableStateFlow<NewToken?>(null)
    private val authTokenflow: StateFlow<NewToken?> = _authTokenChannel

    override fun getAuthToken(): Flow<NewToken?> = authTokenflow

    override suspend fun linkWithPhoneCredentials(userId: String, verificationId: String, smsCode: String) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun linkWithGoogleCredentials(userId: String, googleIdToken: String) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun signInAnonymously() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun signInWithCredential(credential: AuthCredential) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    suspend fun offerUser(delay: Long, user: AuthUser?) {
        coroutineScope {
            launch {
                delay(delay)
                _authUserChannel.value = user
            }
        }
    }

    suspend fun offerToken(token: NewToken?) {
        _authTokenChannel.value = token
    }
}

val anAuthToken = AuthToken("token")
val anAuthUser = AuthUser("uid", false, "name", "photo", "email", "phone")
val anNewAuthToken = NewToken(anAuthToken, anAuthUser)
val anAnonymousAuthUser = AuthUser("uid", true, null, null, null, null)
val anAuthenticatedState = AuthState.Authenticated(
    anAuthToken,
    anAuthUser
)
val anAnonymousAuthenticatedState = AuthState.Authenticated(
    anAuthToken,
    anAnonymousAuthUser
)
