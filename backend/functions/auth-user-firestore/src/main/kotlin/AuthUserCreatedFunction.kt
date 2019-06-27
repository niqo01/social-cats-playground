package com.nicolasmilliard.socialcats

import com.google.cloud.functions.Context
import com.nicolasmilliard.socialcats.store.StoreUser
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

/**
 * Function triggered on new Auth user (/databases/(default)/documents/users/{id}) modifications.
 */
class AuthUserCreatedFunction(
    graph: Graph = AppComponent().build()
) {
    private val store = graph.store

    fun onUserCreated(rawEvent: RawUserRecord, context: Context) {
        log.debug {
            "Event ID: ${context.eventId()}, Resource: ${context.resource()}," +
                " Event Type: ${context.eventType()}, Timestamp: ${context.timestamp()}"
        }
        log.info { "On Auth User created, Event: $rawEvent" }

        requireNotNull(rawEvent.uid)

        val isAnonymous = rawEvent.providerData == null
        // We don't save anonymous users
        if (isAnonymous) return

        val user = StoreUser(
            rawEvent.uid,
            rawEvent.displayName,
            rawEvent.phoneNumber,
            rawEvent.email,
            rawEvent.emailVerified,
            rawEvent.photoURL
        )
        store.createUser(user)
    }
}
