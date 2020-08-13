package com.nicolasmilliard.socialcats.payment

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

actual interface PaymentService {

    @GET("v1/payments/subscriptionDetail")
    actual suspend fun getSubscriptionDetail(
        @Header("Authorization") authToken: String,
        @Query("currency") currency: String?
    ): SubscriptionDetailResult

    @POST("v1/payments/createSubscription")
    actual suspend fun createSubscription(
        @Header("Authorization") authToken: String,
        @Body request: CreateSubscriptionRequest
    ): CreateSubscriptionResult

    @POST("v1/payments/cancelSubscription")
    actual suspend fun cancelSubscription(@Header("Authorization") authToken: String): CancelSubscriptionResult

    @POST("v1/payments/createCheckoutSession")
    actual suspend fun createCheckoutSession(@Header("Authorization") authToken: String): CreateCheckoutSessionResult
}
