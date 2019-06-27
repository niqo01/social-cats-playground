package com.nicolasmilliard.socialcats.searchapi

import com.google.firebase.auth.FirebaseAuthException
import com.nicolasmilliard.socialcats.search.repository.FakeSearchRepository
import com.nicolasmilliard.socialcats.search.repository.SearchRepository
import org.elasticsearch.client.RestHighLevelClient

class TestAppModule : AppModule() {
    override fun provideFirebaseTokenVerifier(): FirebaseTokenVerifier = FakeFirebaseAuth()
    override fun provideSearchRepository(client: RestHighLevelClient): SearchRepository = FakeSearchRepository()
}

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
