package com.nicolasmilliard.socialcats

import com.google.cloud.functions.Context
import com.nicolasmilliard.socialcats.store.User
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
            "Event ID: ${context.eventId()}, Resource: ${context.resource()}, Event Type: ${context.eventType()}, Timestamp: ${context.timestamp()}"
        }
        log.info { "On Auth User created, Event: $rawEvent" }

        requireNotNull(rawEvent.uid)

        val user = User(rawEvent.displayName)
        store.createUser(rawEvent.uid, user)
    }
}
