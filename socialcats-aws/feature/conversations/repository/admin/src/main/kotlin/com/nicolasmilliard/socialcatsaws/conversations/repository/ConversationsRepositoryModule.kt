package com.nicolasmilliard.socialcatsaws.conversations.repository

import com.nicolasmilliard.socialcatsaws.conversations.repository.dynamodb.ConversationsDynamoDb
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

internal object ConversationsRepositoryModule {

  internal fun provideSocialCatsRepository(tableName: String): ConversationsRepository {
    val asyncClient = DynamoDbAsyncClient.create()
    return ConversationsDynamoDb(asyncClient, tableName)
  }
}
