package com.nicolasmilliard.socialcatsaws.conversations.model

import kotlinx.datetime.LocalDate

public data class Conversation(
  val id: String,
  val title: String
)

public data class Message(
  val id: String,
  val conversationId: String,
  val content: String,
  val sentAt: LocalDate
)

public data class ConversationWithMessage(
  val conversation: Conversation,
  val messages: List<Message>
)
