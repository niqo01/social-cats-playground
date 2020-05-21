package com.nicolasmilliard.socialcats.payment

import com.stripe.model.Customer
import com.stripe.model.PaymentMethod
import com.stripe.net.RequestOptions
import com.stripe.param.CustomerCreateParams
import com.stripe.param.CustomerUpdateParams
import com.stripe.param.PaymentMethodAttachParams
import com.stripe.param.SubscriptionCancelParams
import com.stripe.param.SubscriptionCreateParams
import com.stripe.param.SubscriptionListParams
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

typealias StripeSubscription = com.stripe.model.Subscription

class StripeProcessor(
    private val stripePublishableKey: String,
    private val stripeSecretKey: String,
    override val currency: String = US_CURRENCY
) : PaymentProcessor {

    override suspend fun createCustomer(uId: String, email: String?, phoneNumber: String?) = suspendCoroutine<String> {
        val options = createRequestOption().build()

        val params = CustomerCreateParams.builder()
            .setMetadata(mapOf("uid" to uId))
            .setEmail(email)
            .setPhone(phoneNumber)
            .build()

        val customer = Customer.create(params, options)
        it.resume(customer.id)
    }

    override suspend fun getActiveSubscriptions(customerId: String): List<String> = suspendCoroutine {
        val options = createRequestOption().build()

        val params = SubscriptionListParams.builder()
            .setCustomer(customerId)
            .setStatus(SubscriptionListParams.Status.ACTIVE)
            .build()
        val results = StripeSubscription.list(params, options)
        it.resume(results.data.map { s -> s.id })
    }

    override suspend fun createSubscription(
        customerId: String,
        paymentMethodId: String,
        priceId: String
    ): Subscription {
        val options = createRequestOption().build()
        val customer = Customer.retrieve(customerId, options)
        attachPaymentMethod(customer, paymentMethodId, options)
        setDefaultPaymentMethod(customer, paymentMethodId, options)
        val subs = createSubscription(customer, priceId, options)
        log.debug { "subscription: $subs" }
        val invoiceId = subs.latestInvoiceObject.id
        val intent = subs.latestInvoiceObject.paymentIntentObject
        return Subscription(
            subs.id,
            subs.status.toSubscriptionStatus(),
            Invoice(invoiceId, intent.status.toPaymentStatus(), intent.clientSecret)
        )
    }

    // Attach the payment method to the customer
    private suspend fun attachPaymentMethod(
        customer: Customer,
        paymentMethodId: String,
        options: RequestOptions
    ) = suspendCoroutine<Unit> {
        val pm = PaymentMethod.retrieve(paymentMethodId, options)
        pm.attach(PaymentMethodAttachParams.builder().setCustomer(customer.id).build(), options)
        it.resume(Unit)
    }

    // Change the default invoice settings on the customer to the new payment method
    private suspend fun setDefaultPaymentMethod(
        customer: Customer,
        paymentMethodId: String,
        options: RequestOptions
    ) = suspendCoroutine<Unit> {
        val customerParams = CustomerUpdateParams.builder()
            .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder().setDefaultPaymentMethod(paymentMethodId).build())
            .build()
        customer.update(customerParams, options)
        it.resume(Unit)
    }

    private suspend fun createSubscription(customer: Customer, priceId: String, options: RequestOptions) =
        suspendCoroutine<StripeSubscription> {
            val params = SubscriptionCreateParams.builder()
                .setCustomer(customer.id)
                .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
                .addExpand("latest_invoice.payment_intent")
                .build()
            val subscription = StripeSubscription.create(params, options)
            it.resume(subscription)
        }

    override suspend fun cancelSubscription(
        subscriptionId: String
    ): SubscriptionStatus = suspendCoroutine {
        val options = createRequestOption().build()
        var subscription = StripeSubscription.retrieve(subscriptionId, options)

        val params = SubscriptionCancelParams.builder().setProrate(true).build()
        subscription = subscription.cancel(params, options)
        it.resume(subscription.status.toSubscriptionStatus())
    }

    private fun createRequestOption(): RequestOptions.RequestOptionsBuilder {
        return RequestOptions.builder()
            .setApiKey(stripeSecretKey)
    }

    private fun String.toSubscriptionStatus(): SubscriptionStatus = when (this) {
        "active" -> SubscriptionStatus.ACTIVE
        "canceled" -> SubscriptionStatus.CANCELED
        "incomplete" -> SubscriptionStatus.INCOMPLETE
        "incomplete_expired" -> SubscriptionStatus.INCOMPLETE_EXPIRED
        "past_due" -> SubscriptionStatus.PAST_DUE
        "trialing" -> SubscriptionStatus.TRIALING
        "unpaid" -> SubscriptionStatus.UNPAID
        else -> throw IllegalStateException("Unsupported subscription status: $this")
    }
}
