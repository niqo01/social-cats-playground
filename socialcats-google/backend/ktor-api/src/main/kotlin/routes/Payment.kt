package com.nicolasmilliard.socialcats.searchapi.routes

import com.nicolasmilliard.socialcats.api.ApiError
import com.nicolasmilliard.socialcats.api.ApiErrors
import com.nicolasmilliard.socialcats.payment.CreateSubscriptionRequest
import com.nicolasmilliard.socialcats.payment.ERROR_CATEGORY_PAYMENT
import com.nicolasmilliard.socialcats.payment.ERROR_CODE_CUSTOMNER_NOT_FOUND
import com.nicolasmilliard.socialcats.payment.NoCustomerFoundException
import com.nicolasmilliard.socialcats.payment.StripePayments
import com.nicolasmilliard.socialcats.searchapi.FirebaseAuthKey
import com.nicolasmilliard.socialcats.searchapi.PrincipalToken
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post

fun Route.paymentSubscriptionDetail(stripePayments: StripePayments) = authenticate(FirebaseAuthKey) {
    get("/v1/payments/subscriptionDetail") {
        val currency: String? = call.request.queryParameters["currency"]
        val result = stripePayments.getSubscriptionDetail(currency)
        call.respond(result)
    }
}

fun Route.paymentCreateCheckoutSession(stripePayments: StripePayments) = authenticate(FirebaseAuthKey) {
    post("/v1/payments/createCheckoutSession") {
        val uid = call.authentication.principal<PrincipalToken>()!!.value.uid
        try {
            val result = stripePayments.createCheckoutSession(uid)
            call.respond(result)
        } catch (e: NoCustomerFoundException) {
            call.application.environment.log.error("Error while creating session", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiErrors(listOf(ApiError(ERROR_CATEGORY_PAYMENT, ERROR_CODE_CUSTOMNER_NOT_FOUND)))
            )
        }
    }
}

fun Route.paymentCreateSubscription(stripePayments: StripePayments) = authenticate(FirebaseAuthKey) {
    post("/v1/payments/createSubscription") {
        val uid = call.authentication.principal<PrincipalToken>()!!.value.uid
        val request = call.receive<CreateSubscriptionRequest>()
        try {
            val result = stripePayments.createSubscription(uid, request.paymentMethodId, request.priceId)
            call.respond(result)
        } catch (e: NoCustomerFoundException) {
            call.application.environment.log.error("Error while creating subscription", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiErrors(listOf(ApiError(ERROR_CATEGORY_PAYMENT, ERROR_CODE_CUSTOMNER_NOT_FOUND)))
            )
        }
    }
}

fun Route.paymentCancelSubscription(stripePayments: StripePayments) = authenticate(FirebaseAuthKey) {
    post("/v1/payments/cancelSubscription") {
        val uid = call.authentication.principal<PrincipalToken>()!!.value.uid
        try {
            val result = stripePayments.cancelSubscription(uid)
            call.respond(result)
        } catch (e: NoCustomerFoundException) {
            call.application.environment.log.error("Error while canceling subscription", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiErrors(listOf(ApiError(ERROR_CATEGORY_PAYMENT, ERROR_CODE_CUSTOMNER_NOT_FOUND)))
            )
        }
    }
}
