package com.nicolasmilliard.socialcatsaws.conversations.admin.repository.s3

import app.cash.tempest2.BeginsWith
import com.nicolasmilliard.socialcatsaws.conversations.Conversation
import com.nicolasmilliard.socialcatsaws.conversations.admin.repository.ConversationsRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

class ConversationsDynamoDb(private val table: ConversationTable) : ConversationsRepository {
  override fun generateData() {
    val id = UUID.randomUUID().toString()
    val newConversation = ConversationInfo(
      id, "Title", "Description",
      Clock.System.now().toLocalDateTime(
        TimeZone.UTC
      ).date
    )
    table.conversationInfo.save(newConversation)
    val newMessage =
      Message(
        id,
        UUID.randomUUID().toString(),
        "This is a message content",
        "Nicolas",
        Clock.System.now().toLocalDateTime(
          TimeZone.UTC
        ).date
      )
    table.message.save(newMessage)
    val newMessage2 =
      Message(
        id,
        UUID.randomUUID().toString(),
        "Another message please",
        "Nicolas",
        Clock.System.now().toLocalDateTime(
          TimeZone.UTC
        ).date
      )
    table.message.save(newMessage2)
  }

  // Query.
  override fun getConversations(): List<Conversation> {
    return table.conversationInfo.scan().contents.map { it.toConversation() }
  }

  // Load.
  fun getConversationDescription(conversationId: String): String? {
    val key = ConversationInfo.Key(conversationId)
    val albumInfo = table.conversationInfo.load(key) ?: return null
    return albumInfo.description
  }

  // Query.
  fun getMessageContent(conversationId: String): List<String> {
    val page = table.message.query(
      keyCondition = BeginsWith(Message.Key(conversationId))
    )
    return page.contents.map { it.content }
  }

  fun ConversationInfo.toConversation() =
    Conversation(id = conversation_id, createdAt = started_date)
}
