package com.nicolasmilliard.socialcats.store

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import java.util.concurrent.TimeUnit.SECONDS
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SocialCatsFirestoreAdmin(private val firestore: Firestore) {

    fun createUser(uid: String, user: User) {
        logger.debug { "createUser($uid, $user)" }
        val result = firestore.collection("users").document(uid).set(user).get(30, SECONDS)
        logger.info { "User Created: $uid at time: ${result.updateTime}" }
    }

    fun deleteUser(uid: String) {
        logger.debug { "deleteUser($uid)" }
        val result = firestore.collection("users").document(uid).delete().get(30, SECONDS)
        logger.info { "User Deleted: $uid at time: ${result.updateTime}" }
    }
}

// Adding default value for Firestore serialization
data class User(
    val name: String? = null,
    val createdAt: FieldValue = FieldValue.serverTimestamp()
)
