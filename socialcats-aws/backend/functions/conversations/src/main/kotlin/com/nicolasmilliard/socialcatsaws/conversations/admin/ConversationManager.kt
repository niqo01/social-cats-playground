package com.nicolasmilliard.socialcatsaws.conversations.admin

import com.nicolasmilliard.socialcatsaws.conversations.Conversation
import com.nicolasmilliard.socialcatsaws.conversations.admin.repository.ConversationsRepository

class ConversationManager(private val repository: ConversationsRepository) {

  fun generateData() {
    repository.generateData()
  }

  fun getConversations(): List<Conversation> {
    return repository.getConversations()
  }
}
