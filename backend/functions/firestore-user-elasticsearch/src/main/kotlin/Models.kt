package com.nicolasmilliard.socialcats

import java.util.Date

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
