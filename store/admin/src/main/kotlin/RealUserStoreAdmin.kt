package com.nicolasmilliard.socialcats.store

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import com.nicolasmilliard.socialcats.store.DbConstants.Collections.Users
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalTime::class)
class RealUserStoreAdmin(private val db: Firestore) : UserStoreAdmin {

    override suspend fun createUser(user: InsertUser) = withTimeout(60.seconds) {
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

        val result = db
            .collection(Users.NAME)
            .document(user.uid)
            .set(data)
            .await()
        logger.info { "User Created: ${user.uid} at time: ${result.updateTime}" }
    }

    override suspend fun getCustomerId(uId: String): String? {
        val doc = db
            .collection(Users.NAME)
            .document(uId)
            .collection(Users.PaymentProcessor.NAME)
            .document(Users.PaymentProcessor.Processor.STRIPE.id)
            .get().await()
        return if (doc.exists()) {
            doc.getString(Users.PaymentProcessor.Fields.CUSTOMER_ID)
        } else {
            null
        }
    }

    override suspend fun setPaymentInfo(uId: String, customerUId: String) = withTimeout(60.seconds) {
        logger.debug { "setPaymentInfo($uId)" }
        val result = db
            .collection(Users.NAME)
            .document(uId)
            .collection(Users.PaymentProcessor.NAME)
            .document(Users.PaymentProcessor.Processor.STRIPE.id)
            .set(mapOf(Users.PaymentProcessor.Fields.CUSTOMER_ID to customerUId))
            .await()
        logger.info { "Payment Info Created: $uId at time: ${result.updateTime}" }
    }

    override suspend fun setMembershipStatus(uId: String, active: Boolean) {
        logger.debug { "setMembershipStatus($uId)" }
        val result = db
            .collection(Users.NAME)
            .document(uId)
            .update(Users.Fields.MEMBERSHIP_STATUS, active)
            .await()
        logger.info { "User membership updated: $uId at time: ${result.updateTime}" }
    }
}
