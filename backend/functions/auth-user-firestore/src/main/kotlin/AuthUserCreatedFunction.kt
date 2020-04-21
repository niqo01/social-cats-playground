package com.nicolasmilliard.socialcats

import com.google.cloud.functions.Context
import com.google.cloud.functions.RawBackgroundFunction
import com.nicolasmilliard.socialcats.store.InsertUser
import com.squareup.moshi.Moshi
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

/**
 * Function triggered on new Auth user (/databases/(default)/documents/users/{id}) modifications.
 */
class AuthUserCreatedFunction(graph: Graph = AppComponent().build()) : RawBackgroundFunction {

    private val userConverter = UserConverter(graph.moshi)
    private val store = graph.store

    init {
        graph.appInitializer.initialize()
    }

    override fun accept(json: String, context: Context) {
        try {
            log.debug {
                "Event ID: ${context.eventId()}, Resource: ${context.resource()}," +
                    " Event Type: ${context.eventType()}, Timestamp: ${context.timestamp()}"
            }
            log.debug { "json: $json" }

            val userRecord = userConverter.convert(json)
            log.info { "On Auth User created, Event: $userRecord" }

            val isAnonymous = userRecord.providerData == null
            // We don't save anonymous users
            if (isAnonymous) return

            val userInfo = userRecord.providerData!![0]
            val user =
                InsertUser(
                    userRecord.uid,
                    userRecord.displayName ?: userInfo.displayName,
                    userRecord.phoneNumber ?: userInfo.phoneNumber,
                    userRecord.email ?: userInfo.email,
                    userRecord.emailVerified,
                    userRecord.photoURL ?: userInfo.photoURL
                )
            store.createUser(user)
        } catch (e: Exception) {
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
