package com.nicolasmilliard.socialcats.payment

import com.nicolasmilliard.socialcats.api.bearer
import com.nicolasmilliard.socialcats.util.IO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class PaymentLoader(
    private val service: PaymentService,
    private val stripeService: StripeService
) {

    fun loadSubscriptionDetail(authToken: String) = execute(authToken) {
        service.getSubscriptionDetail(it, null)
    }

    fun createSubscription(
        authToken: String,
        paymentMethodId: String,
        priceId: String
    ) = execute(authToken) {
        service.createSubscription(it, CreateSubscriptionRequest(paymentMethodId, priceId))
    }

    fun cancelSubscription(
        authToken: String
    ) = execute(authToken) {
        service.cancelSubscription(it)
    }

    fun createPaymentMethod(
        card: StripeCard
    ) = flow {
        emit(Status.InProgress)
        try {
            val result = stripeService.createPaymentMethod(card)
            emit(Status.Success(result))
        } catch (exception: Throwable) {
            emit(Status.Failure(exception))
        }
    }

    fun onPaymentResult(
        requestCode: Int,
        data: Any?
    ) = flow {
        emit(Status.InProgress)
        try {
            val result = stripeService.onPaymentResult(requestCode, data)
            emit(Status.Success(result))
        } catch (exception: Throwable) {
            emit(Status.Failure(exception))
        }
    }

    private inline fun <R> execute(authToken: String, crossinline operation: suspend (bearer: String) -> R) = flow {
        emit(Status.InProgress)
        try {
            val bearer = bearer(authToken)
            val result = withContext(Dispatchers.IO()) { operation(bearer) }
            emit(Status.Success(result))
        } catch (exception: Throwable) {
            emit(Status.Failure(exception))
        }
    }

    sealed class Status<out T> {
        object InProgress : Status<Nothing>()
        data class Success<T>(val data: T) : Status<T>()
        data class Failure(val exception: Throwable) : Status<Nothing>()
    }
}
