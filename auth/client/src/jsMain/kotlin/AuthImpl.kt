package com.nicolasmilliard.socialcats.auth

import kotlinx.coroutines.flow.Flow

class AuthImpl : Auth {
    override suspend fun linkWithGoogleCredentials(googleIdToken: String) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun getAuthState(): Flow<AuthState> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
