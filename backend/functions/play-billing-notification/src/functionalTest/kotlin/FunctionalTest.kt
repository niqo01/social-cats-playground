package com.nicolasmilliard.socialcats

import org.junit.Test

class FunctionalTest {

    // Looks like Firebase adming does not support custom endpoint yet
//    private class TestComponent {
//        val fakeInitializer = object : Initializer {
//            override fun initialize() {
//            }
//        }
//
//        fun build(): Graph {
//            val appModule = AppModule()
//            val firestore = appModule.provideFirestore()
//
//            val settings: FirestoreSettings = FirestoreSettings.newBuilder()
//                .setEndpoint("10.0.2.2:8282")
//                .build()
//            firestore.setFirestoreSettings(settings)
//            val store = appModule.provideSocialCatsFirestoreAdmin(firestore)
//            val moshi = appModule.provideMoshi()
//            return Graph(store, moshi, fakeInitializer)
//        }
//    }

    @Test
    fun testRoot() {

        val context = FakeContext()
//        val testComponent = TestComponent()

        val function = AuthUserCreatedFunction()

        val json =
            """
            {
                "displayName": "Nicolas Testing Fun",
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

        function.accept(json, context)
    }

    class FakeContext : com.google.cloud.functions.Context {
        override fun eventId(): String = "eventId"
        override fun timestamp(): String = "timestamp"
        override fun eventType(): String = "eventType"
        override fun resource(): String? = "resource"
    }
}
