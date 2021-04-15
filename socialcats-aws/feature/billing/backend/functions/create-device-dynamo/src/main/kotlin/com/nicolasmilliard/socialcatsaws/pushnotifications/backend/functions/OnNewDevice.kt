package com.nicolasmilliard.socialcatsaws.pushnotifications.backend.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.nicolasmilliard.socialcatsaws.profile.model.Device
import com.nicolasmilliard.socialcatsaws.profile.model.DeviceIdProvider
import com.nicolasmilliard.socialcatsaws.profile.model.SupportedPlatform
import com.nicolasmilliard.socialcatsaws.pushnotification.models.IdProvider
import com.nicolasmilliard.socialcatsaws.pushnotification.models.Platform
import com.nicolasmilliard.socialcatsaws.pushnotification.models.SendRegTokenRequest
import com.nicolasmilliard.socialcatsaws.pushnotification.models.SendRegTokenResult
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import mu.withLoggingContext

private val logger = KotlinLogging.logger {}

class OnNewDevice(appComponent: AppComponent = DaggerAppComponent.create()) :
  RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private val json = Json

  private val cloudMetrics = appComponent.getCloudMetrics()
  private val useCase = appComponent.getNewDeviceUseCase()

  override fun handleRequest(
    event: APIGatewayV2HTTPEvent,
    context: Context
  ): APIGatewayV2HTTPResponse {
    cloudMetrics.putProperty("RequestId", context.awsRequestId)
    val userId = event.requestContext.authorizer.jwt.claims["sub"]!!
    withLoggingContext("UserID" to userId) {
      val sendRegRequest: SendRegTokenRequest = json.decodeFromString(event.body)
      val device = Device(
        userId,
        sendRegRequest.instanceId,
        Clock.System.now(),
        sendRegRequest.token,
        sendRegRequest.provider.toModel(),
        sendRegRequest.platform.toModel(),
        sendRegRequest.appVersionCode,
        sendRegRequest.languageTag,
      )
      useCase.onNewDevice(device)
      val headers = mapOf("Content-Type" to "application/json")
      val response = APIGatewayV2HTTPResponse.builder()
        .withStatusCode(200)
        .withHeaders(headers)
        .withBody(
          json.encodeToString(SendRegTokenResult)
        )
        .withIsBase64Encoded(false)
        .build()
      cloudMetrics.flush()
      return response
    }
  }

  fun IdProvider.toModel(): DeviceIdProvider = when (this) {
    IdProvider.FCM -> DeviceIdProvider.FCM
    else -> throw kotlin.IllegalArgumentException("Unsupported Id provider: $this")
  }

  fun Platform.toModel(): SupportedPlatform = when (this) {
    Platform.ANDROID -> SupportedPlatform.ANDROID
    else -> throw kotlin.IllegalArgumentException("Unsupported Id provider: $this")
  }
}
