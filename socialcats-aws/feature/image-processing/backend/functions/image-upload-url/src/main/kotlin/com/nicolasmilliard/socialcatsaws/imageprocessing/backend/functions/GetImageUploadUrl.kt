package com.nicolasmilliard.socialcatsaws.imageprocessing.backend.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.nicolasmilliard.cloudmetric.CloudMetricModule
import com.nicolasmilliard.socialcatsaws.imageupload.CreateUploadUrlResult
import com.nicolasmilliard.socialcatsaws.imageupload.MaxStoredImagesReached
import com.nicolasmilliard.socialcatsaws.imageupload.PreSignedRequest
import com.nicolasmilliard.socialcatsaws.imageupload.UploadData
import com.nicolasmilliard.socialcatsaws.profile.ImageUseCaseModule
import com.nicolasmilliard.socialcatsaws.profile.model.CreateSignedUrl
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.withLoggingContext

class GetImageUploadUrl : RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private val cloudMetrics = CloudMetricModule.provideCloudMetrics(System.getenv("APP_NAME"))

  private val useCase = ImageUseCaseModule.provideUploadImageUseCase(
    System.getenv("DDB_TABLE_NAME"),
    System.getenv("S3_BUCKET_NAME"),
    cloudMetrics,
    System.getenv("AWS_REGION")
  )
  private val json = Json

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
