package com.nicolasmilliard.socialcatsaws.imageupload

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

public interface ImageUploadApi {
  @POST("v1/images")
  public suspend fun getUploadUrl(
    @Header("Authorization") token: String
  ): CreateUploadUrlResult

  @PUT
  public suspend fun uploadImage(@Url url: String, @HeaderMap headers: Map<String, String>, @Body file: RequestBody)
}
