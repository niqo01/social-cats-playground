package com.nicolasmilliard.socialcats.auth

import com.google.cloud.functions.Context
import com.google.common.truth.Truth.assertThat
import com.nicolasmilliard.socialcats.store.FakeUserStoreAdmin
import mu.KotlinLogging
import org.junit.Test

private val log = KotlinLogging.logger {}

internal class FirestoreEventFunctionTest {

    private class TestComponent {
        val fakeUserStore = FakeUserStoreAdmin()
        val fakeInitializer = object : Initializer {
            override fun initialize() {
            }
        }

        fun build(): Graph {
            val appModule = AppModule()
            val moshi = appModule.provideMoshi()
            val useCase = appModule.provideNewUserUseCase(fakeUserStore)
            return Graph(useCase, moshi, fakeInitializer)
        }
    }

    @Test
    fun `on user created event`() {

        val json = """
            {
                "displayName": "Nicolas Milliard",
                "metadata": {
                    "createdAt": "2020-03-12T17:40:02Z",
                    "lastSignedInAt": "2020-03-12T17:40:02Z"
                },
                "photoURL": "https://lh3.googleusercontent.com/a-/AOh14GhQMPFX-xnquD6iztU29XR9EHOYtDxTO-0uBNzNvQ\u003ds96-c",
                "providerData": [{
                    "displayName": "Nicolas Milliard",
                    "email": "niqo01@gmail.com",
                    "photoURL": "https://lh3.googleusercontent.com/a-/AOh14GhQMPFX-xnquD6iztU29XR9EHOYtDxTO-0uBNzNvQ\u003ds96-c",
                    "providerId": "google.com",
                    "uid": "106927208431194134382"
                }],
                "uid": "siybyGbgIeSfOCwM0FE1l4d8Abk2"
            }
        """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        PlayBillingNotification(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeUserStore.insertedUsers).isNotEmpty()
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
                "metadata": {"createdAt": "2002-10-02T10:00:00-05:00", "lastSignedInAt": "2002-10-02T10:00:00-05:00"},
                "providerData": null
            }
        """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        PlayBillingNotification(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeUserStore.insertedUsers).isEmpty()
    }

    class FakeContext : Context {
        override fun timestamp() = "timestamp"

        override fun eventId() = "eventId"

        override fun resource() = "resource"

        override fun eventType() = "eventType"
    }
}
