package com.nicolasmilliard.socialcats.auth

import kotlinx.coroutines.flow.Flow

interface Auth {
    fun getAuthState(): Flow<AuthState>
    suspend fun linkWithGoogleCredentials(googleIdToken: String)
    suspend fun signInAnonymously()
    suspend fun signInWithCredential(credential: AuthCredential)
}

sealed class AuthState {

    object UnAuthenticated : AuthState()

    data class Authenticated(
        val authToken: AuthToken,
        val authUser: AuthUser
    ) : AuthState()
}

data class AuthUser(
    val uid: String,
    val isAnonymous: Boolean,
    val displayName: String?,
    val photoUrl: String?,
    val email: String?,
    val phoneNumber: String?
)

data class AuthToken(
    val token: String
)

expect abstract class AuthCredential
