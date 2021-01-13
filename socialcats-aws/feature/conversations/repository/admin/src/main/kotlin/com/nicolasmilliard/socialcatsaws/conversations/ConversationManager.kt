package com.nicolasmilliard.socialcatsaws.conversations

import com.nicolasmilliard.socialcatsaws.conversations.model.Conversation
import com.nicolasmilliard.socialcatsaws.conversations.model.Message
import com.nicolasmilliard.socialcatsaws.conversations.repository.ConversationsRepository
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
public class ConversationManager internal constructor(private val repository: ConversationsRepository) {

  public suspend fun addConversation(conversation: Conversation) {
    logger.debug("addConversation()")
    repository.insertConversation(conversation)
  }

  public suspend fun addMessage(message: Message) {
    logger.debug("addMessage()")
    repository.insertMessage(message)
  }
}
