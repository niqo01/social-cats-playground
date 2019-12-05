package com.nicolasmilliard.socialcats.store

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import com.nicolasmilliard.socialcats.store.DbConstants.Collections.Users
import java.util.concurrent.TimeUnit.SECONDS
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class RealUserStoreAdmin(private val firestore: Firestore) : UserStoreAdmin {

    override fun createUser(user: InsertUser) {
        logger.debug { "createUser($user)" }
        require(!user.name.isNullOrBlank() || !user.email.isNullOrBlank() || !user.phoneNumber.isNullOrBlank())
        val data = mapOf<String, Any?>(
            Users.Fields.CREATED_AT to FieldValue.serverTimestamp(),
            Users.Fields.NAME to user.name,
            Users.Fields.PHONE_NUMBER to user.phoneNumber,
            Users.Fields.PHOTO_URL to user.photoUrl,
            Users.Fields.EMAIL to user.email,
            Users.Fields.EMAIL_VERIFIED to user.emailVerified
        )

        val result = firestore
            .collection(Users.NAME)
            .document(user.uid)
            .set(data).get(30, SECONDS)
        logger.info { "User Created: ${user.uid} at time: ${result.updateTime}" }
    }
}
