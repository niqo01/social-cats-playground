package com.nicolasmilliard.socialcatsaws.conversations.repository

import com.nicolasmilliard.socialcatsaws.conversations.model.Conversation
import com.nicolasmilliard.socialcatsaws.conversations.model.ConversationWithMessage
import com.nicolasmilliard.socialcatsaws.conversations.model.Message

internal interface ConversationsRepository {
  suspend fun getConversationAndMessages(conversationId: String, newestMessagesCount: Int): ConversationWithMessage

  suspend fun insertConversation(conversation: Conversation)
  suspend fun insertMessage(message: Message)

  suspend fun deleteAllItems()
}
