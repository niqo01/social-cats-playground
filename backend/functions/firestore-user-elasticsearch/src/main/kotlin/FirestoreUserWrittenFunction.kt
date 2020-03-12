package com.nicolasmilliard.socialcats

import com.google.cloud.functions.Context
import com.google.cloud.functions.RawBackgroundFunction
import com.nicolasmilliard.socialcats.search.repository.IndexUser
import com.nicolasmilliard.socialcats.search.repository.SearchConstants.Index
import com.nicolasmilliard.socialcats.store.DbConstants.Collections.Users
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Function triggered on Firestore documents (/databases/(default)/documents/users/{id}) modifications.
 */
class FirestoreUserWrittenFunction(
    graph: Graph = AppComponent().build()
) : RawBackgroundFunction {

    private val moshi = graph.moshi
    private val searchUseCase = graph.searchUseCase

    override fun accept(json: String, context: Context) = runBlocking {
        logger.debug {
            "Event ID: ${context.eventId()}, Resource: ${context.resource()}, " +
                "Event Type: ${context.eventType()}, Timestamp: ${context.timestamp()}\n" +
                "Json content: $json"
        }

        val jsonAdapter: JsonAdapter<FirestoreEvent> = moshi.adapter(FirestoreEvent::class.java)
        val event: FirestoreEvent = checkNotNull(jsonAdapter.fromJson(json))

        logger.info { "On User written, Event: $event" }

        when (event) {
            is CreatedEvent -> indexUser(event.value)
            is UpdatedEvent -> {
                indexUser(event.value)
            }
            is DeletedEvent -> searchUseCase.deleteUser(event.resourceId)
        }
    }

    private suspend fun indexUser(value: FirestoreValue) {
        val indexFields = mutableMapOf<String, Any?>()

        value.fields.forEach { (k, v) ->
            when (k) {
                Users.Fields.NAME -> indexFields[Index.Users.Fields.NAME] = v
                Users.Fields.PHOTO_URL -> indexFields[Index.Users.Fields.PHOTO_URL] = v
                Users.Fields.EMAIL_VERIFIED -> indexFields[Index.Users.Fields.EMAIL_VERIFIED] = v
                Users.Fields.EMAIL -> indexFields[Index.Users.Fields.EMAIL] = v
                Users.Fields.PHONE_NUMBER -> indexFields[Index.Users.Fields.PHONE_NUMBER] = v
                else -> logger.warn { "Ignoring field: $k" }
            }
        }

        if (indexFields.isEmpty()) {
            logger.info { "This user change is not interesting for us: ${value.fields.keys.joinToString()}}" }
        } else {
            searchUseCase.indexUser(IndexUser(value.resourceId, value.createTime, indexFields))
        }
    }
}
