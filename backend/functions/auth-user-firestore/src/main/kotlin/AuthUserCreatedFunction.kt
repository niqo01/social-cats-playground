package com.nicolasmilliard.socialcats

import com.google.cloud.functions.Context
import com.google.cloud.functions.RawBackgroundFunction
import com.nicolasmilliard.socialcats.store.InsertUser
import com.squareup.moshi.JsonAdapter
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

/**
 * Function triggered on new Auth user (/databases/(default)/documents/users/{id}) modifications.
 */
class AuthUserCreatedFunction(
    graph: Graph = AppComponent().build()
) : RawBackgroundFunction {

    private val moshi = graph.moshi
    private val store = graph.store

    override fun accept(json: String, context: Context) {
        log.debug {
            "Event ID: ${context.eventId()}, Resource: ${context.resource()}," +
                " Event Type: ${context.eventType()}, Timestamp: ${context.timestamp()}"
        }

        val jsonAdapter: JsonAdapter<UserRecord> = moshi.adapter(UserRecord::class.java)
        val userRecord: UserRecord = checkNotNull(jsonAdapter.fromJson(json))

        log.info { "On Auth User created, Event: $userRecord" }

        val isAnonymous = userRecord.providerData == null
        // We don't save anonymous users
        if (isAnonymous) return

        val user =
            InsertUser(
                userRecord.uid,
                userRecord.displayName,
                userRecord.phoneNumber,
                userRecord.email,
                userRecord.emailVerified,
                userRecord.photoURL
            )
        store.createUser(user)
    }
}
