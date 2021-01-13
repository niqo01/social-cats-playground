package com.nicolasmilliard.socialcatsaws.conversations.admin.functions

import app.cash.tempest2.LogicalDb
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.nicolasmilliard.socialcatsaws.conversations.Conversation
import com.nicolasmilliard.socialcatsaws.conversations.admin.ConversationManager
import com.nicolasmilliard.socialcatsaws.conversations.admin.repository.ConversationsRepository
import com.nicolasmilliard.socialcatsaws.conversations.admin.repository.s3.ConversationsDb
import com.nicolasmilliard.socialcatsaws.conversations.admin.repository.s3.ConversationsDynamoDb
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient

private val logger = KotlinLogging.logger {}
class GetConversations : RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private val manager: ConversationManager
  private val json = Json { prettyPrint = true }

  init {
    val client = DynamoDbEnhancedClient.create()
    val db: ConversationsDb = LogicalDb.create(ConversationsDb::class, client)
    val repository: ConversationsRepository = ConversationsDynamoDb(db.conversations)
    manager = ConversationManager(repository)
    manager.generateData()
  }

  override fun handleRequest(
    input: APIGatewayV2HTTPEvent,
    context: Context
  ): APIGatewayV2HTTPResponse {
    logger.debug("Input: $input")

    val conversations = manager.getConversations()
    val result = GetConversationsResult(conversations)

    val headers = mapOf("Content-Type" to "application/json")
    val response = APIGatewayV2HTTPResponse.builder()
      .withStatusCode(200)
      .withHeaders(headers)
      .withBody(
        json.encodeToString(result)
      )
      .withIsBase64Encoded(false)
      .build()
    logger.debug("Response: $response")
    return response
  }
}

@Serializable
data class GetConversationsResult(
  val conversations: List<Conversation>
)
