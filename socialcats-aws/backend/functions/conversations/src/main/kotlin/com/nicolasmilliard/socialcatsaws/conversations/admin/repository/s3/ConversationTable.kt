package com.nicolasmilliard.socialcatsaws.conversations.admin.repository.s3

import app.cash.tempest2.Attribute
import app.cash.tempest2.InlineView
import app.cash.tempest2.LogicalTable
import kotlinx.datetime.LocalDate

interface ConversationTable : LogicalTable<ConversationItem> {
  val conversationInfo: InlineView<ConversationInfo.Key, ConversationInfo>
  val message: InlineView<Message.Key, Message>
}

data class ConversationInfo(
  @Attribute(name = "partition_key")
  val conversation_id: String,
  val conversation_title: String,
  val description: String,
  val started_date: LocalDate
) {
  @Attribute(prefix = "INFO_")
  val sort_key: String = ""

  @Transient
  val key = Key(conversation_id)

  data class Key(
    val conversation_id: String
  ) {
    val sort_key: String = ""
  }
}

data class Message(
  @Attribute(name = "partition_key")
  val conversation_id: String,
  @Attribute(name = "sort_key", prefix = "MESSAGE_")
  val message_id: String,
  val content: String,
  val author_name: String,
  val created_date: LocalDate
) {

  @Transient
  val key = Key(conversation_id, message_id)

  data class Key(
    val conversation_id: String,
    val message_id: String = ""
  )
}
