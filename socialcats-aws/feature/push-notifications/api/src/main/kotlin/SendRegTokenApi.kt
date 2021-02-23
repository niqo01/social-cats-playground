package com.nicolasmilliard.socialcatsaws.pushnotification

import com.nicolasmilliard.socialcatsaws.pushnotification.models.SendRegTokenRequest
import com.nicolasmilliard.socialcatsaws.pushnotification.models.SendRegTokenResult
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

public interface SendRegTokenApi {
  @POST("v1/devices")
  public suspend fun sendToken(
    @Header("Authorization") token: String,
    @Body request: SendRegTokenRequest
  ): SendRegTokenResult
}
