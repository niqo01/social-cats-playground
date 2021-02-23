package com.nicolasmilliard.socialcatsaws.profile.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.nicolasmilliard.cloudmetric.Unit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import mu.withLoggingContext
import java.io.InputStream
import java.io.OutputStream

private val logger = KotlinLogging.logger {}

class OnNewAuthUser(appComponent: AppComponent = DaggerAppComponent.create()) : RequestStreamHandler {

  private val cloudMetrics = appComponent.getCloudMetrics()
  private val useCase = appComponent.getNewUserUseCase()
  private val json = Json

  override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {

    output.bufferedWriter().use { outputBuffer ->
      val inputText = input.bufferedReader().use { it.readText() }

      cloudMetrics.putProperty("RequestId", context.awsRequestId)

      val event: CognitoEvent = json.decodeFromString(inputText)
      if (event.triggerSource == TRIGGER_SOURCE_POST_CONFIRM_SIGN_UP ||
        event.triggerSource == TRIGGER_SOURCE_POST_ADMIN_CONFIRM_SIGN_UP ||
        event.triggerSource == TRIGGER_SOURCE_POST_SIGN_UP
      ) {
        val userAttr = event.request.userAttributes
        if (userAttr.cognitoUserStatus == COGNITO_USER_STATUS_CONFIRMED) {

          withLoggingContext("UserId" to userAttr.sub) {
            logger.info("event=auth_new_user")
            cloudMetrics.putMetric("AuthNewUserCount", 1.0, Unit.COUNT)
            useCase.onNewAuthUser(
              userAttr.sub,
              userAttr.email,
              userAttr.email_verified.toBoolean()
            )
          }
        }
      }

      outputBuffer.write(inputText)
    }
    cloudMetrics.flush()
  }
}

const val TRIGGER_SOURCE_POST_CONFIRM_SIGN_UP = "PostConfirmation_ConfirmSignUp"
const val TRIGGER_SOURCE_POST_ADMIN_CONFIRM_SIGN_UP = "PostConfirmation_AdminConfirmSignUp"
const val TRIGGER_SOURCE_POST_SIGN_UP = "PostConfirmation_SignUp"

@Serializable
data class CognitoEvent(
  val version: String,
  val region: String,
  val userPoolId: String,
  val userName: String,
  val callerContext: CallerContext,
  val triggerSource: String,
  val request: Request,
  val response: Response
)

@Serializable
object Response

@Serializable
data class CallerContext(
  val awsSdkVersion: String,
  val clientId: String,
)

@Serializable
data class Request(
  val userAttributes: UserAttributes
)

const val COGNITO_USER_STATUS_CONFIRMED = "CONFIRMED"

@Serializable
data class UserAttributes(
  val sub: String,
  @SerialName("cognito:email_alias")
  val cognitoEmailAlias: String,
  @SerialName("cognito:user_status")
  val cognitoUserStatus: String,
  val email_verified: String,
  val email: String,
)
