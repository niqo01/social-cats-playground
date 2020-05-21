package com.nicolasmilliard.socialcats.payment

expect interface PaymentService {
    suspend fun getSubscriptionDetail(authToken: String, currency: String?): SubscriptionDetailResult
    suspend fun createSubscription(authToken: String, request: CreateSubscriptionRequest): CreateSubscriptionResult
    suspend fun cancelSubscription(authToken: String): CancelSubscriptionResult
}

interface StripeService {
    suspend fun createPaymentMethod(card: StripeCard): NewPaymentMethodResult
    suspend fun onPaymentResult(requestCode: Int, data: Any?): PaymentStatus
}

expect class StripeCard

sealed class NewPaymentMethodResult {
    data class Success(val methodId: String) : NewPaymentMethodResult()
    data class Failure(val code: String, val message: String?) : NewPaymentMethodResult()
}
