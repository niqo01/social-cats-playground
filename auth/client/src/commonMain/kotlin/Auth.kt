package com.nicolasmilliard.socialcats.auth

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import mu.KotlinLogging

interface AuthProvider {
    fun getAuthUser(): Flow<AuthUser?>
    fun getAuthToken(): Flow<NewToken?>
    suspend fun linkWithPhoneCredentials(userId: String, verificationId: String, smsCode: String)
    suspend fun linkWithGoogleCredentials(userId: String, googleIdToken: String)
    suspend fun signInAnonymously()
    suspend fun signInWithCredential(credential: AuthCredential)
}

private val logger = KotlinLogging.logger {}

class Auth(private val authProvider: AuthProvider) {
    private val _authState = ConflatedBroadcastChannel<AuthState>()
    val authStates: Flow<AuthState> get() = _authState.asFlow()

    suspend fun start() {
        coroutineScope {
            launch {
                getAuthState()
                    .onEach { logger.info { "$it" } }
                    .collect { _authState.offer(it) }
            }
        }
    }

    private fun getAuthState(): Flow<AuthState> {
        return authProvider.getAuthUser()
            .flatMapLatest { authUser ->
                if (authUser == null) {
                    flowOf(AuthState.UnAuthenticated)
                } else {
                    authProvider.getAuthToken().map {
                        if (it == null) {
                            AuthState.UnAuthenticated
                        } else {
                            AuthState.Authenticated(it.token, it.authUser)
                        }
                    }.onStart { emit(AuthState.Authenticated(null, authUser)) }
                }
            }
    }

    suspend fun linkWithGoogleCredentials(googleIdToken: String) {
        authProvider.linkWithGoogleCredentials(googleIdToken)
    }

    suspend fun signInAnonymously() {
        authProvider.signInAnonymously()
    }

    suspend fun signInWithCredential(credential: AuthCredential) {
        authProvider.signInWithCredential(credential)
    }
}

sealed class AuthState {

    object UnAuthenticated : AuthState()

    data class Authenticated(
        val authToken: AuthToken?,
        val authUser: AuthUser
    ) : AuthState()

    override fun toString(): String {
        return when (this) {
            is UnAuthenticated -> "UnAuthenticated"
            is Authenticated -> "Authenticated (${this.authUser.uid}), AuthUser anonymous?: ${this.authUser.isAnonymous}"
        }
    }
}

data class AuthUser(
    val uid: String,
    val isAnonymous: Boolean,
    val hasPhoneAuth: Boolean,
    val displayName: String?,
    val photoUrl: String?,
    val email: String?,
    val phoneNumber: String?
)

data class AuthToken(
    val token: String
)

// FirebaseAuth does not trigger Auth change when signing up after account Delete, but Token change listener is.
data class NewToken(
    val token: AuthToken?,
    val authUser: AuthUser
)

expect abstract class AuthCredential
class AuthRecentLoginRequiredException(cause: Throwable) : Exception(cause)
