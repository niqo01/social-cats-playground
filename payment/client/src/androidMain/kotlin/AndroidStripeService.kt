package com.nicolasmilliard.socialcats.payment

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.exception.InvalidRequestException
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AndroidStripeService(
    private val context: Context,
    private val publishableKey: String
) : StripeService {

    private lateinit var stripe: Stripe

    fun init() {
        stripe = Stripe(context, publishableKey)
    }

    override suspend fun createPaymentMethod(card: StripeCard): NewPaymentMethodResult = suspendCoroutine {
        val callback = object : ApiResultCallback<PaymentMethod> {
            override fun onError(e: Exception) {
                if (e is InvalidRequestException && e.stripeError!!.type == "invalid_request_error") {
                    val stripeError = e.stripeError!!
                    it.resume(NewPaymentMethodResult.Failure(stripeError.code!!, stripeError.message))
                } else {
                    it.resumeWithException(e)
                }
            }

            override fun onSuccess(method: PaymentMethod) {
                it.resume(NewPaymentMethodResult.Success(method.id!!))
            }
        }

        val params = PaymentMethodCreateParams.create(card = card)
        stripe.createPaymentMethod(params, callback = callback)
    }

    fun confirmCardPayment(fragment: Fragment, paymentMethodId: String, clientSecret: String) {
        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(paymentMethodId, clientSecret)
        stripe.confirmPayment(fragment, params)
    }

    override suspend fun onPaymentResult(requestCode: Int, data: Any?): PaymentStatus = suspendCoroutine {
        val callback = object : ApiResultCallback<PaymentIntentResult> {
            override fun onError(e: Exception) {
                it.resumeWithException(e)
            }

            override fun onSuccess(result: PaymentIntentResult) {
                val paymentIntent = result.intent
                val status = paymentIntent.status
                it.resume(status!!.code!!.toPaymentStatus())
            }
        }

        stripe.onPaymentResult(requestCode, data as Intent?, callback)
    }
}

actual typealias StripeCard = PaymentMethodCreateParams.Card
