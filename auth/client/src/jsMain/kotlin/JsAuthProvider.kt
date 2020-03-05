package com.nicolasmilliard.socialcats.auth

import kotlinx.coroutines.flow.Flow

class JsAuthProvider : AuthProvider {
    override fun getAuthUser(): Flow<AuthUser?> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun getAuthToken(): Flow<NewToken?> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun linkWithPhoneCredentials(userId: String, verificationId: String, smsCode: String) {
        TODO("Not yet implemented")
    }

    override suspend fun linkWithGoogleCredentials(userId: String, googleIdToken: String) {
        TODO("Not yet implemented")
    }


    override suspend fun signInAnonymously() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun signInWithCredential(credential: AuthCredential) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}

actual abstract class AuthCredential
