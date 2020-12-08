package com.nicolasmilliard.socialcats.payment

import java.util.UUID

@OptIn(ExperimentalStdlibApi::class)
class FakePaymentProcessor(override val currency: String = US_CURRENCY) : PaymentProcessor {
    val customersCreated = mutableSetOf<String>()
    val subscriptionsCreationResult = ArrayDeque<Subscription>()
    val subscriptionsCancelResult = ArrayDeque<SubscriptionStatus>()
    val activeSubs = mutableListOf<String>()

    override suspend fun createCustomer(
        uId: String,
        email: String?,
        phoneNumber: String?
    ): String {
        val customerId = UUID.randomUUID().toString()
        customersCreated.add(customerId)
        return customerId
    }

    override suspend fun createCheckoutSession(
        customerId: String,
        productId: String,
        currency: String,
        amount: Long
    ): String {
        return "sessionId"
    }

    override suspend fun createSubscription(
        customerId: String,
        paymentMethodId: String,
        priceId: String
    ): Subscription = subscriptionsCreationResult.removeFirst()

    override suspend fun cancelSubscription(subscriptionId: String) = subscriptionsCancelResult.removeFirst()

    override suspend fun getActiveSubscriptions(customerId: String): List<String> = activeSubs.toList()
}
