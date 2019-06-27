package com.nicolasmilliard.socialcats

import com.google.cloud.functions.Context
import com.google.gson.JsonElement
import com.nicolasmilliard.socialcats.search.repository.IndexUser
import com.nicolasmilliard.socialcats.search.repository.SearchConstants.Index
import com.nicolasmilliard.socialcats.store.DbConstants.Collections.Users
import java.util.Date
import mu.KotlinLogging
import org.apache.logging.log4j.util.Strings

private val logger = KotlinLogging.logger {}

/**
 * Function triggered on Firestore documents (/databases/(default)/documents/users/{id}) modifications.
 */
class FirestoreUserWrittenFunction(
    graph: Graph = AppComponent().build()
) {
    private val searchUseCase = graph.searchUseCase

    fun onUserWritten(rawEvent: RawFirestoreEvent, context: Context) {
        logger.debug {
            "Event ID: ${context.eventId()}, Resource: ${context.resource()}, " +
                "Event Type: ${context.eventType()}, Timestamp: ${context.timestamp()}"
        }
        logger.info { "On User written, Event: $rawEvent" }

        when (val event = rawEvent.map()) {
            is CreatedEvent -> indexUser(event.value)
            is UpdatedEvent -> {
                indexUser(event.value)
            }
            is DeletedEvent -> searchUseCase.deleteUser(event.resourceId)
        }
    }

    private fun indexUser(value: FirestoreValue) {
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

// Below are models serialized automatically from Gson.
// Due to Gson limited Kotlin support, be warn of:
// - making sure all properties are nullable
// - no default value are assigned to property
// - no delegate properties are used
// More information here: https://medium.com/@programmerr47/gson-unsafe-problem-d1ff29d4696f

data class RawFirestoreValue(
    val createTime: Date?,
    val updateTime: Date?,
    val name: String?,
    val fields: JsonElement?
)

data class RawFirestoreEvent(
    val oldValue: RawFirestoreValue?,
    val value: RawFirestoreValue?,
    val updateMask: RawUpdateMask?
)

data class RawUpdateMask(
    val fieldPaths: Set<String>?
)

// Below are Kotlin friendly models we will use to map our raw model into.

sealed class FirestoreEvent

data class CreatedEvent(
    val value: FirestoreValue
) : FirestoreEvent()

data class UpdatedEvent(
    val oldValue: FirestoreValue,
    val value: FirestoreValue,
    val updatedFields: Set<String>
) : FirestoreEvent()

data class DeletedEvent(
    val resourceId: String
) : FirestoreEvent()

data class FirestoreValue(
    val resourceId: String,
    val createTime: Date,
    val updateTime: Date,
    val fields: Map<String, Any>
)

private fun RawFirestoreEvent.map(): FirestoreEvent {
    checkNotNull(oldValue)
    checkNotNull(value)
    return when {
        oldValue.name != null && value.name == null -> {
            val resourceId = oldValue.parseResourceId()
            check(!resourceId.isNullOrEmpty())
            DeletedEvent(resourceId)
        }
        oldValue.name == null && value.name != null -> {
            CreatedEvent(value.map())
        }
        else -> {
            checkNotNull(updateMask)
            checkNotNull(updateMask.fieldPaths)
            check(updateMask.fieldPaths.isNotEmpty())
            UpdatedEvent(oldValue.map(), value.map(), updateMask.fieldPaths)
        }
    }
}

private fun RawFirestoreValue.map(): FirestoreValue {
    checkNotNull(createTime)
    checkNotNull(updateTime)
    checkNotNull(name)
    checkNotNull(fields)

    val resourceId = parseResourceId()
    check(!resourceId.isNullOrEmpty())

    val resultMap = HashMap<String, Any>()
    val fieldsJsonObject = fields.asJsonObject
    fieldsJsonObject.keySet().forEach {
        val fieldValues = fieldsJsonObject.getAsJsonObject(it)
        if (fieldValues.has("stringValue")) {
            resultMap[it] = fieldValues["stringValue"].asString
        }
        // Complete other type if necessary
    }
    return FirestoreValue(resourceId, createTime, updateTime, resultMap)
}

private fun RawFirestoreValue.parseResourceId() = name?.substringAfterLast("/", Strings.EMPTY)
