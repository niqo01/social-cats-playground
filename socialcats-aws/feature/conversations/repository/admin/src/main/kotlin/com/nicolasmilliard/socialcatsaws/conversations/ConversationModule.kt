package com.nicolasmilliard.socialcatsaws.conversations

import com.nicolasmilliard.socialcatsaws.conversations.repository.ConversationsRepositoryModule

public object ConversationModule {

  public fun provideUpdateUser(tableName: String): ConversationManager {
    val repository = ConversationsRepositoryModule.provideSocialCatsRepository(tableName)
    return ConversationManager(repository)
  }
}
