package com.nicolasmilliard.socialcats

import com.google.cloud.functions.Context
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

/**
 * Function triggered on new Auth user (/databases/(default)/documents/users/{id}) modifications.
 */
class AuthUserDeletedFunction(
    graph: Graph = AppComponent().build()
) {
    private val store = graph.store

    fun onUserDeleted(rawEvent: RawUserRecord, context: Context) {
        log.debug {
            "Event ID: ${context.eventId()}, Resource: ${context.resource()}, " +
                "Event Type: ${context.eventType()}, Timestamp: ${context.timestamp()}"
        }
        log.info { "On Auth User deleted, Event: $rawEvent" }

        requireNotNull(rawEvent.uid)
        val isAnonymous = rawEvent.providerData == null
        // We don't save anonymous users
        if (isAnonymous) return

        store.deleteUser(rawEvent.uid)
    }
}
