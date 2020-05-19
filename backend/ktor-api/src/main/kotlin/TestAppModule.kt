package com.nicolasmilliard.socialcats.searchapi

import com.google.firebase.auth.FirebaseAuthException

const val TEST_VALID_TOKEN = "validToken"

class FakeFirebaseAuth : FirebaseTokenVerifier {

    override fun verifyIdToken(token: String): Token {
        return if (token == TEST_VALID_TOKEN) Token(
            uid = "uid",
            isEmailVerified = false,
            claims = mapOf("sub" to "validToken")
        ) else throw FirebaseAuthException("error_code", "Error message")
    }
}
