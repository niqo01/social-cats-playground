package com.nicolasmilliard.socialcats

import com.nicolasmilliard.socialcats.search.FirestoreValue

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
