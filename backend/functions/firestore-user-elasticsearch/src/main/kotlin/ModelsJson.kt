package com.nicolasmilliard.socialcats

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import java.util.Date

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class RawFields

@JsonClass(generateAdapter = true)
data class FirestoreValueJson(
    val createTime: Date?,
    val updateTime: Date?,
    val name: String?,
    @RawFields val fields: Map<String, Any>?
)

@JsonClass(generateAdapter = true)
data class UpdateMaskJson(
    val fieldPaths: Set<String>
)

@JsonClass(generateAdapter = true)
data class FirestoreEventJson(
    val oldValue: FirestoreValueJson,
    val value: FirestoreValueJson,
    val updateMask: UpdateMaskJson?
)

internal class FieldsAdapter {
    @FromJson
    @RawFields
    fun fromJson(reader: JsonReader): Map<String, Any>? {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.skipValue()
            return null
        }
        val map = HashMap<String, Any>()

        reader.beginObject() // fields
        while (reader.hasNext()) {
            val name = reader.nextName() // prop name
            reader.beginObject()
            while (reader.hasNext()) {
                val type = reader.nextName()
                if (type == "stringValue") {
                    map[name] = reader.nextString()
                    break
                } else {
                    reader.skipValue()
                }
            }
            reader.endObject()
        }
        reader.endObject()

        if (map.size == 0) {
            return null
        }

        return map.toMap()
    }

    @ToJson
    fun toJson(@RawFields value: Map<String, @JvmSuppressWildcards Any>): String {
        throw UnsupportedOperationException()
    }
}

private fun String.parseResourceId(): String = this.substringAfterLast("/", "")

internal class FirestoreValueJsonAdapter {
    @FromJson
    fun fromJson(valueJson: FirestoreValueJson): FirestoreValue? {
        if (valueJson.name == null) {
            return null
        }
        val resourceId = valueJson.name.parseResourceId()
        checkNotNull(resourceId.isNotEmpty())
        checkNotNull(valueJson.createTime)
        checkNotNull(valueJson.updateTime)
        checkNotNull(valueJson.fields)

        return FirestoreValue(resourceId, valueJson.createTime, valueJson.createTime, valueJson.fields)
    }

    @ToJson
    fun toJson(value: FirestoreValue): String {
        throw UnsupportedOperationException()
    }
}

internal class FirestoreEventJsonAdapter(val firestoreValueAdapter: FirestoreValueJsonAdapter) {
    @FromJson
    fun fromJson(valueJson: FirestoreEventJson): FirestoreEvent {
        return when {
            valueJson.oldValue.name != null && valueJson.value.name == null -> {
                val resourceId = valueJson.oldValue.name.parseResourceId()
                check(resourceId.isNotEmpty())
                DeletedEvent(resourceId)
            }
            valueJson.oldValue.name == null && valueJson.value.name != null -> {
                CreatedEvent(firestoreValueAdapter.fromJson(valueJson.value)!!)
            }
            else -> {
                checkNotNull(valueJson.updateMask)
                check(valueJson.updateMask.fieldPaths.isNotEmpty())
                UpdatedEvent(firestoreValueAdapter.fromJson(valueJson.oldValue)!!,
                    firestoreValueAdapter.fromJson(valueJson.value)!!,
                    valueJson.updateMask.fieldPaths)
            }
        }
    }

    @ToJson
    fun toJson(value: FirestoreEvent): String {
        throw UnsupportedOperationException()
    }
}
