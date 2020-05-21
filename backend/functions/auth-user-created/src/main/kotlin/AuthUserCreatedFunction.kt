package com.nicolasmilliard.socialcats.auth

import com.google.cloud.functions.Context
import com.google.cloud.functions.RawBackgroundFunction
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

/**
 * Function triggered on new Auth user (/databases/(default)/documents/users/{id}) modifications.
 */
class AuthUserCreatedFunction(graph: Graph = AppComponent().build()) : RawBackgroundFunction {

    private val userConverter = UserConverter(graph.moshi)
    private val newUserHandler = graph.newUserHandler

    init {
        graph.appInitializer.initialize()
    }

    override fun accept(json: String, context: Context) = runBlocking {
        try {
            log.debug {
                "Event ID: ${context.eventId()}, Resource: ${context.resource()}," +
                    " Event Type: ${context.eventType()}, Timestamp: ${context.timestamp()}"
            }
            log.debug { "json: $json" }

            val userRecord = userConverter.convert(json)
            log.info { "On Auth User created, Event: $userRecord" }

            newUserHandler(userRecord)
        } catch (e: Throwable) {
            log.error(e) { "Error while processing event" }
            throw e
        }
    }

    private class UserConverter(moshi: Moshi) {

        private val adapter = moshi.adapter(UserRecord::class.java)

        fun convert(json: String): UserRecord {
            return checkNotNull(adapter.fromJson(json))
        }
    }
}
