package com.nicolasmilliard.socialcats

import com.google.cloud.functions.Context
import com.google.common.truth.Truth.assertThat
import com.nicolasmilliard.socialcats.store.InsertUser
import com.nicolasmilliard.socialcats.store.UserStoreAdmin
import mu.KotlinLogging
import org.junit.Test

private val log = KotlinLogging.logger {}

internal class FirestoreEventFunctionTest {

    private class TestComponent {
        val fakeUserStore = FakeUserStore()

        fun build(): Graph {
            val appModule = AppModule()
            val moshi = appModule.provideMoshi()
            return Graph(fakeUserStore, moshi)
        }
    }

    @Test
    fun `on user created event`() {
        val json = """
            {
                "uid": "uid",
                "disabled": false,
                "displayName": "Toto",
                "email": "asd@d.com",
                "emailVerified": true,
                "phoneNumber": null,
                "photoURL": "https://asd.asd.com/img.jpg",
                "metadata": {"creationTime": "2002-10-02T10:00:00-05:00", "lastSignInTime": "2002-10-02T10:00:00-05:00"},
                "providerData": [{"providerId": "google","uid": "uid"}]
            }
        """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        AuthUserCreatedFunction(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeUserStore.wasCreatedCalled).isTrue()
    }

    @Test
    fun `on anonymous user event`() {
        val json = """
            {
                "uid": "uid",
                "disabled": false,
                "displayName": null,
                "email": null,
                "emailVerified": false,
                "phoneNumber": null,
                "photoURL": null,
                "metadata": {"creationTime": "2002-10-02T10:00:00-05:00", "lastSignInTime": "2002-10-02T10:00:00-05:00"},
                "providerData": null
            }
        """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        AuthUserCreatedFunction(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeUserStore.wasCreatedCalled).isFalse()
    }

    class FakeContext : Context {
        override fun timestamp() = "timestamp"

        override fun eventId() = "eventId"

        override fun resource() = "resource"

        override fun eventType() = "eventType"
    }

    class FakeUserStore : UserStoreAdmin {
        var wasCreatedCalled: Boolean = false
        override fun createUser(user: InsertUser) {
            if (wasCreatedCalled) IllegalStateException("updateUserName called more than once")
            wasCreatedCalled = true
        }
    }
}
