package com.nicolasmilliard.socialcats.auth

import kotlinx.coroutines.flow.Flow

class JsAuthProvider : AuthProvider {
    override fun getAuthUser(): Flow<AuthUser?> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun getAuthToken(): Flow<AuthToken?> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun linkWithGoogleCredentials(googleIdToken: String) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun signInAnonymously() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun signInWithCredential(credential: AuthCredential) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}

actual abstract class AuthCredential
