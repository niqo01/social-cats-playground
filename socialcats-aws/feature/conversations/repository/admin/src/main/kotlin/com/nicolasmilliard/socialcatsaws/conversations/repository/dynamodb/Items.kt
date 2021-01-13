package com.nicolasmilliard.socialcatsaws.repository.admin.dynamodb

import com.nicolasmilliard.socialcatsaws.conversations.model.Conversation
import com.nicolasmilliard.socialcatsaws.conversations.model.Message
import kotlinx.datetime.LocalDate
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

internal const val CONVERSATION_TYPE = "conversation"
internal const val CONVERSATION_RECORD = "B"
@DynamoDbBean
internal data class ConversationItem(
  @get:DynamoDbAttribute("conversation_Id")
  var id: String? = null,
  var title: String? = null
) {
  @get:DynamoDbPartitionKey
  var partition_key get() = prefixedId(id)
    set(_) {
      // Ignore needed by enhanced client
    }

  @get:DynamoDbSortKey
  var sort_key get() = CONVERSATION_RECORD
    set(_) {
      // Ignore needed by enhanced client
    }

  var item_type get() = CONVERSATION_TYPE
    set(_) {
      // Ignore needed by enhanced client
    }

  companion object {
    fun prefixedId(id: String?): String {
      return "conversation#$id"
    }
  }
}
internal fun ConversationItem.toConversation() = Conversation(id!!, title!!)
internal fun Conversation.toConversationItem() = ConversationItem(id, title)

internal const val MESSAGE_TYPE = "message"
@DynamoDbBean
internal data class MessageItem(
  @get:DynamoDbAttribute("message_Id")
  var id: String? = null,
  var conversationId: String? = null,
  var content: String? = null,
  @get:DynamoDbConvertedBy(LocalDateTypeConverter::class)
  var sentAt: LocalDate? = null
) {
  @get:DynamoDbPartitionKey
  var partition_key get() = ConversationItem.prefixedId(conversationId)
    set(_) {
      // Ignore needed by enhanced client
    }

  @get:DynamoDbSortKey
  var sort_key get() = prefixedId(conversationId)
    set(_) {
      // Ignore needed by enhanced client
    }

  var item_type get() = MESSAGE_TYPE
    set(_) {
      // Ignore needed by enhanced client
    }

  companion object {
    fun prefixedId(id: String?): String {
      return "message#$id"
    }
  }
}
internal fun MessageItem.toMessage() = Message(id!!, conversationId!!, content!!, sentAt!!)
internal fun Message.toMessageItem() = MessageItem(id, conversationId, content, sentAt)

internal class LocalDateTypeConverter : AttributeConverter<LocalDate> {
  override fun transformTo(input: AttributeValue): LocalDate {
    return LocalDate.parse(input.s())
  }

  override fun transformFrom(localDate: LocalDate): AttributeValue {
    return AttributeValue.builder().s(localDate.toString()).build()
  }

  override fun type(): EnhancedType<LocalDate> {
    return EnhancedType.of(LocalDate::class.java)
  }

  override fun attributeValueType(): AttributeValueType {
    return AttributeValueType.S
  }
}
