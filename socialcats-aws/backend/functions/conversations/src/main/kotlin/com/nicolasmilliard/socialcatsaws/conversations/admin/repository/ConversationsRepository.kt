package com.nicolasmilliard.socialcatsaws.conversations.admin.repository

import com.nicolasmilliard.socialcatsaws.conversations.Conversation

interface ConversationsRepository {

  fun generateData()
  fun getConversations(): List<Conversation>
}
