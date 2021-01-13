package com.nicolasmilliard.socialcatsaws.conversations.admin.repository.s3

import app.cash.tempest2.LogicalDb
import app.cash.tempest2.TableName

interface ConversationsDb : LogicalDb {
  @TableName("conversations")
  val conversations: ConversationTable
}
