package com.nicolasmilliard.socialcatsaws.conversations.repository.dynamodb

import com.nicolasmilliard.socialcatsaws.conversations.model.Conversation
import com.nicolasmilliard.socialcatsaws.conversations.model.ConversationWithMessage
import com.nicolasmilliard.socialcatsaws.conversations.model.Message
import com.nicolasmilliard.socialcatsaws.conversations.repository.ConversationsRepository
import com.nicolasmilliard.socialcatsaws.conversations.repository.DbEntityAlreadyExistsException
import com.nicolasmilliard.socialcatsaws.conversations.repository.DbInvalidEntityException
import com.nicolasmilliard.socialcatsaws.repository.admin.dynamodb.CONVERSATION_TYPE
import com.nicolasmilliard.socialcatsaws.repository.admin.dynamodb.ConversationItem
import com.nicolasmilliard.socialcatsaws.repository.admin.dynamodb.MESSAGE_TYPE
import com.nicolasmilliard.socialcatsaws.repository.admin.dynamodb.MessageItem
import com.nicolasmilliard.socialcatsaws.repository.admin.dynamodb.toConversation
import com.nicolasmilliard.socialcatsaws.repository.admin.dynamodb.toConversationItem
import com.nicolasmilliard.socialcatsaws.repository.admin.dynamodb.toMessage
import com.nicolasmilliard.socialcatsaws.repository.admin.dynamodb.toMessageItem
import kotlinx.coroutines.future.await
import mu.KotlinLogging
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.QueryResponse
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.ScanResponse

private val logger = KotlinLogging.logger {}
internal class ConversationsDynamoDb(private val client: DynamoDbAsyncClient, private val tableName: String) :
  ConversationsRepository {
  private val enhancedClient = DynamoDbEnhancedAsyncClient.builder()
    .dynamoDbClient(client)
    .build()

  private val conversationTableSchema = TableSchema.fromClass(ConversationItem::class.java)
  private val conversationTable = enhancedClient.table(tableName, conversationTableSchema)

  private val messageTableSchema = TableSchema.fromClass(MessageItem::class.java)
  private val messageTable = enhancedClient.table(tableName, messageTableSchema)

  override suspend fun getConversationAndMessages(conversationId: String, newestMessagesCount: Int): ConversationWithMessage {
    logger.debug("getConversationAndMessages()")
    val conversationPk: AttributeValue = AttributeValue.builder()
      .s(ConversationItem.prefixedId(conversationId))
      .build()
    val queryRequest = QueryRequest.builder()
      .tableName(tableName) // Define aliases for the Attribute, '#pk' and the value, ':pk'
      .keyConditionExpression("#pk = :pk") // '#pk' refers to the Attribute 'PK'
      .expressionAttributeNames(
        java.util.Map.of(
          "#pk",
          "partition_key"
        )
      ) // ':pk' refers to the customer PK of interest
      .expressionAttributeValues(
        java.util.Map.of(
          ":pk",
          conversationPk
        )
      ) // Search from "bottom to top"
      .scanIndexForward(false) // One customer, plus N newest orders
      .limit(1 + newestMessagesCount)
      .build()

    // Use the DynamoDbClient directly rather than the
    // DynamoDbEnhancedClient or DynamoDbTable

    val queryResponse: QueryResponse = client.query(queryRequest).await()
    // The result is a list of items in a "DynamoDB JSON map"
    val items: List<Map<String, AttributeValue>> = queryResponse.items()
    var conversation: Conversation? = null
    val messages = mutableListOf<Message>()
    items.forEach {
      val type = it["item_type"]

      if (type == null || type.s().isNullOrEmpty()) {
        throw DbInvalidEntityException("Required attribute 'Type' is missing or empty on Item with attributes: $it")
      }
      when (type.s()) {
        CONVERSATION_TYPE ->
          conversation =
            conversationTableSchema.mapToItem(it).toConversation()
        MESSAGE_TYPE -> messages.add(messageTableSchema.mapToItem(it).toMessage())
        else -> throw DbInvalidEntityException("Found unhandled Type=${type.s()} on Item with attributes: $it")
      }
    }
    if (conversation == null) {
      throw DbInvalidEntityException("No conversation found in query result")
    }
    return ConversationWithMessage(conversation!!, messages)
  }

  override suspend fun insertConversation(conversation: Conversation) {
    logger.debug("insertConversation()")
    val item = conversation.toConversationItem()
    val expression = Expression.builder()
      .expression("attribute_not_exists(PK)")
      .build()
    val putRequest = PutItemEnhancedRequest.builder(ConversationItem::class.java)
      .item(item)
      .conditionExpression(expression)
      .build()
    try {
      conversationTable.putItem(putRequest).await()
    } catch (e: ConditionalCheckFailedException) {
      throw DbEntityAlreadyExistsException("Attempted to overwrite an item which already exists with PK=${item.partition_key}")
    }
  }

  override suspend fun insertMessage(message: Message) {
    logger.debug("insertMessage()")
    this.messageTable.putItem(message.toMessageItem()).await()
  }

  override suspend fun deleteAllItems() {
    logger.warn("deleteAllItems()")
    val scanRequest = ScanRequest.builder()
      .tableName(tableName)
      .attributesToGet("partition_key", "sort_key")
      .build()
    // Note: Normally, full-table scans should be avoided in DynamoDB
    val scanResponse: ScanResponse = client.scan(scanRequest).await()
    for (item in scanResponse.items()) {
      val deleteItemRequest = DeleteItemRequest.builder()
        .tableName(tableName)
        .key(
          java.util.Map.of(
            "partition_key",
            item["partition_key"],
            "sort_key",
            item["sort_key"]
          )
        )
        .build()
      client.deleteItem(deleteItemRequest).await()
    }
  }
}
