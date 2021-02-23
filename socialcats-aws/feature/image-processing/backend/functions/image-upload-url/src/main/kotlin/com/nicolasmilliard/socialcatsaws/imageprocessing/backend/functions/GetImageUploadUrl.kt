package com.nicolasmilliard.socialcatsaws.imageprocessing.backend.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.nicolasmilliard.socialcatsaws.imageupload.CreateUploadUrlResult
import com.nicolasmilliard.socialcatsaws.imageupload.MaxStoredImagesReached
import com.nicolasmilliard.socialcatsaws.imageupload.PreSignedRequest
import com.nicolasmilliard.socialcatsaws.imageupload.UploadData
import com.nicolasmilliard.socialcatsaws.profile.model.CreateSignedUrl
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.withLoggingContext

class GetImageUploadUrl(appComponent: AppComponent = DaggerAppComponent.create()) : RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private val json = Json

  private val cloudMetrics = appComponent.getCloudMetrics()
  private val useCase = appComponent.getUploadImageUseCase()

  override fun handleRequest(
    event: APIGatewayV2HTTPEvent,
    context: Context
  ): APIGatewayV2HTTPResponse {

    cloudMetrics.putProperty("RequestId", context.awsRequestId)

    val userId = event.requestContext.authorizer.jwt.claims["sub"]!!
    withLoggingContext("UserID" to userId) {
      val signedUrl = useCase.createSignedUrl(userId)
      val signRequest: PreSignedRequest = when (signedUrl) {
        is CreateSignedUrl.CreateSignedUrlData -> UploadData(
          signedUrl.url,
          signedUrl.headers
        )
        is CreateSignedUrl.MaxStoredImagesReached -> MaxStoredImagesReached
      }

      val headers = mapOf("Content-Type" to "application/json")
      val response = APIGatewayV2HTTPResponse.builder()
        .withStatusCode(200)
        .withHeaders(headers)
        .withBody(
          json.encodeToString(CreateUploadUrlResult(signRequest))
        )
        .withIsBase64Encoded(false)
        .build()
      cloudMetrics.flush()
      return response
    }
  }
}
