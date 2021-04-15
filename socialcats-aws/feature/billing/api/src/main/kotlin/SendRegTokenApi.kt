package com.nicolasmilliard.socialcatsaws.billing

import com.nicolasmilliard.socialcatsaws.billing.models.SendPurchaseRequest
import com.nicolasmilliard.socialcatsaws.billing.models.SendPurchaseResult
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

public interface SendRegTokenApi {
  @POST("v1/subscriptions")
  public suspend fun processPurchase(
    @Header("Authorization") token: String,
    @Body request: SendPurchaseRequest
  ): SendPurchaseResult
}
