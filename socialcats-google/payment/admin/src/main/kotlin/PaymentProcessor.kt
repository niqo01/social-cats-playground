package com.nicolasmilliard.socialcats.payment

const val US_CURRENCY = "USD"

interface PaymentProcessor {
    val currency: String
    suspend fun createCheckoutSession(customerId: String, productId: String, currency: String, amount: Long): String
    suspend fun createCustomer(uId: String, email: String?, phoneNumber: String?): String
    suspend fun createSubscription(customerId: String, paymentMethodId: String, priceId: String): Subscription
    suspend fun cancelSubscription(subscriptionId: String): SubscriptionStatus
    suspend fun getActiveSubscriptions(customerId: String): List<String>
}
