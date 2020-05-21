package com.nicolasmilliard.socialcats.payment

import com.nicolasmilliard.socialcats.store.UserStoreAdmin
import mu.KotlinLogging

private val log = KotlinLogging.logger {}
val priceIds = mapOf(
    US_CURRENCY to listOf(
        Price("price_1GtK9nHNVGyWuZEzS03ZWzb6", 100L),
        Price("price_1GtKBCHNVGyWuZEzo7zbY43I", 500L)
    )
)

class Payments(private val paymentProcessor: PaymentProcessor, private val store: UserStoreAdmin) {

    suspend fun createCustomer(uId: String, email: String?, phoneNumber: String?) {
        require(email != null || phoneNumber != null)
        val customerId = paymentProcessor.createCustomer(uId, email, phoneNumber)
        store.setPaymentInfo(uId, customerId)
    }

    suspend fun createSubscription(uId: String, paymentMethodId: String, priceId: String): CreateSubscriptionResult {
        log.info { "createSubscription: $uId" }
        val customerId = store.getCustomerId(uId) ?: throw NoCustomerFoundException(uId)

        var activeSubscription = getActiveSubscription(customerId)
        if (activeSubscription != null) {
            log.warn { "User $uId already has a subscription" }
        } else {
            activeSubscription = paymentProcessor.createSubscription(customerId, paymentMethodId, priceId)
        }

        log.debug { "subscription: $activeSubscription" }
        when (activeSubscription.status) {
            // TODO Try Cloud task on failure
            SubscriptionStatus.ACTIVE -> store.setMembershipStatus(uId, true)
        }
        return CreateSubscriptionResult(activeSubscription)
    }

    fun getSubscriptionDetail(
        currency: String?
    ): SubscriptionDetailResult {
        log.info { "getSubscriptionDetail" }
        val currency = currency ?: US_CURRENCY
        val result = priceIds[currency] ?: priceIds[US_CURRENCY]!!
        return SubscriptionDetailResult(result)
    }

    suspend fun cancelSubscription(uId: String): CancelSubscriptionResult {
        log.info { "createSubscription: $uId" }
        val customerId = store.getCustomerId(uId) ?: throw NoCustomerFoundException(uId)
        val activeSub = getActiveSubscription(customerId)
        val result = if (activeSub == null) {
            log.warn { "Trying to cancel a subscription but none active $uId" }
            SubscriptionStatus.CANCELED
        } else {
            paymentProcessor.cancelSubscription(activeSub.id)
        }

        log.debug { "subscription: $result" }
        if (result != SubscriptionStatus.ACTIVE) {
            store.setMembershipStatus(uId, false)
        }
        return CancelSubscriptionResult(result)
    }

    private suspend fun getActiveSubscription(customerId: String): Subscription? {
        val activeSubs = paymentProcessor.getActiveSubscriptions(customerId)
        return when {
            activeSubs.size == 1 -> {
                Subscription(activeSubs[0], SubscriptionStatus.ACTIVE)
            }
            activeSubs.size > 1 -> {
                throw IllegalStateException("More than one subscription active for customer: $customerId")
            }
            else -> {
                null
            }
        }
    }
}

class NoCustomerFoundException(uId: String) : PaymentException("No Customer found for user id: $uId")
