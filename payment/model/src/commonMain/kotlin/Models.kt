package com.nicolasmilliard.socialcats.payment

import kotlinx.serialization.Serializable

@Serializable
data class StripeError(
    val code: Int,
    val message: String
)

const val ERROR_CATEGORY_PAYMENT = "PAYMENT"
const val ERROR_CODE_CUSTOMNER_NOT_FOUND = "CUSTOMER_NOT_FOUND"
const val ERROR_CODE_CURRENCY_REQUIRED = "CURRENCY_REQUIRED"

@Serializable
data class SubscriptionDetailResult(
    val prices: List<Price>
)

@Serializable
data class CreateCheckoutSessionResult(
    val checkoutUrl: String
)

@Serializable
data class CreateSubscriptionRequest(
    val paymentMethodId: String,
    val priceId: String
)

@Serializable
data class CreateSubscriptionResult(
    val subscription: Subscription
)

@Serializable
data class CancelSubscriptionResult(
    val subsriptionStatus: SubscriptionStatus
)

@Serializable
data class Price(
    val id: String,
    val amount: Long
)

@Serializable
data class Subscription(
    val id: String,
    val status: SubscriptionStatus,
    val invoice: Invoice? = null
)

@Serializable
data class Invoice(
    val id: String,
    val paymentIntent: PaymentIntent?
)

@Serializable
data class PaymentIntent(
    val status: PaymentStatus,
    val clientSecret: String
)

enum class PaymentStatus {
    REQUIRES_PAYMENT_METHOD,
    REQUIRES_PAYMENT_CONFIRMATION,
    REQUIRES_ACTION,
    PROCESSING,
    REQUIRES_CAPTURE,
    CANCELED,
    SUCCEEDED
}

enum class SubscriptionStatus {
    ACTIVE,
    CANCELED,
    INCOMPLETE,
    INCOMPLETE_EXPIRED,
    PAST_DUE,
    TRIALING,
    UNPAID
}

fun String.toPaymentStatus(): PaymentStatus = when (this) {
    "requires_payment_method" -> PaymentStatus.REQUIRES_PAYMENT_METHOD
    "requires_confirmation" -> PaymentStatus.REQUIRES_PAYMENT_CONFIRMATION
    "requires_action" -> PaymentStatus.REQUIRES_ACTION
    "processing" -> PaymentStatus.PROCESSING
    "requires_capture" -> PaymentStatus.REQUIRES_CAPTURE
    "canceled" -> PaymentStatus.CANCELED
    "succeeded" -> PaymentStatus.SUCCEEDED
    else -> throw IllegalStateException("Unsupported payment status: $this")
}
