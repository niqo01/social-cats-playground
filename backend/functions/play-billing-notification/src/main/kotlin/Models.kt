package com.nicolasmilliard.socialcats.auth

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import okio.ByteString.Companion.decodeBase64

@JsonClass(generateAdapter = true)
data class PubSubMessage(
    val data: DeveloperNotification?,
    val attributes: Map<String, String>,
    val messageId: String,
    val publishTime: String
)

// See https://developer.android.com/google/play/billing/rtdn-reference

@JsonClass(generateAdapter = true)
data class DeveloperNotification(
    val version: String,
    val packageName: String,
    val eventTimeMillis: Long,
    val oneTimeProductNotification: OneTimeProductNotification?,
    val subscriptionNotification: SubscriptionNotification?,
    val testNotification: TestNotification?
)

@JsonClass(generateAdapter = true)
data class OneTimeProductNotification(
    val version: String,
    val notificationType: Int,
    val purchaseToken: String,
    val sku: String
)

@JsonClass(generateAdapter = true)
data class SubscriptionNotification(
    val version: String,
    val notificationType: NotificationType,
    val purchaseToken: String,
    val subscriptionId: String
)

@JsonClass(generateAdapter = true)
data class TestNotification(
    val version: String
)

enum class NotificationType {
    SUBSCRIPTION_RECOVERED, // A subscription was recovered from account hold.
    SUBSCRIPTION_RENEWED, // An active subscription was renewed.
    SUBSCRIPTION_CANCELED, // A subscription was either voluntarily or involuntarily cancelled. For voluntary cancellation, sent when the user cancels.
    SUBSCRIPTION_PURCHASED, // A new subscription was purchased.
    SUBSCRIPTION_ON_HOLD, // A subscription has entered account hold (if enabled).
    SUBSCRIPTION_IN_GRACE_PERIOD, // A subscription has entered grace period (if enabled).
    SUBSCRIPTION_RESTARTED, // User has reactivated their subscription from Play > Account > Subscriptions (requires opt//in for subscription restoration).
    SUBSCRIPTION_PRICE_CHANGE_CONFIRMED, // A subscription price change has successfully been confirmed by the user.
    SUBSCRIPTION_DEFERRED, // A subscription's recurrence time has been extended.
    SUBSCRIPTION_PAUSED, // A subscription has been paused.
    SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED, // A subscription pause schedule has been changed.
    SUBSCRIPTION_REVOKED, // A subscription has been revoked from the user before the expiration time.
    SUBSCRIPTION_EXPIRED //  - A subscription has expired.
}

data class DecodedBase64(val value: String)

internal class DecodeBase64StringAdapter {

    @FromJson
    fun fromJson(encoded: String?, delegate: JsonAdapter<DeveloperNotification>): DecodedBase64? {
        if (encoded == null) return null
        return DecodedBase64(encoded.decodeBase64()!!.string(Charsets.UTF_8))
    }
}

internal class DecodedToDeveloperNotificationAdapter {

    @FromJson
    fun fromJson(decoded: DecodedBase64?, delegate: JsonAdapter<DeveloperNotification>): DeveloperNotification? {
        if (decoded == null) return null
        return delegate.fromJson(decoded.value)
    }
}

internal class NotificationTypeAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): NotificationType {
        if (reader.peek() == JsonReader.Token.NULL) {
            throw JsonDataException("Should not be null")
        }
        return when (val type = reader.nextInt()) {
            1 -> NotificationType.SUBSCRIPTION_RECOVERED
            2 -> NotificationType.SUBSCRIPTION_RENEWED
            3 -> NotificationType.SUBSCRIPTION_CANCELED
            4 -> NotificationType.SUBSCRIPTION_PURCHASED
            5 -> NotificationType.SUBSCRIPTION_ON_HOLD
            6 -> NotificationType.SUBSCRIPTION_IN_GRACE_PERIOD
            7 -> NotificationType.SUBSCRIPTION_RESTARTED
            8 -> NotificationType.SUBSCRIPTION_PRICE_CHANGE_CONFIRMED
            9 -> NotificationType.SUBSCRIPTION_DEFERRED
            10 -> NotificationType.SUBSCRIPTION_PAUSED
            11 -> NotificationType.SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED
            12 -> NotificationType.SUBSCRIPTION_REVOKED
            13 -> NotificationType.SUBSCRIPTION_EXPIRED
            else -> throw JsonDataException("Unsupported type: $type")
        }
    }
}

