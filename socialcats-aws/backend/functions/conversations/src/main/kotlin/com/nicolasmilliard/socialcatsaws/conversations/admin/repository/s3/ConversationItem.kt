package com.nicolasmilliard.socialcatsaws.conversations.admin.repository.s3

import kotlinx.datetime.LocalDate
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

@DynamoDbBean
class ConversationItem {
  // All Items.
  @get:DynamoDbPartitionKey
  var partition_key: String? = null

  @get:DynamoDbSortKey
  var sort_key: String? = null

  // ConversationInfo.
  var conversation_title: String? = null

  var description: String? = null

  @get:DynamoDbConvertedBy(LocalDateTypeConverter::class)
  var started_date: LocalDate? = null

  // Message.
  var content: String? = null

  var author_name: String? = null

  @get:DynamoDbConvertedBy(LocalDateTypeConverter::class)
  var created_date: LocalDate? = null
}

internal class LocalDateTypeConverter : AttributeConverter<LocalDate> {
  override fun transformTo(input: AttributeValue): LocalDate {
    return LocalDate.parse(input.s())
  }

  override fun transformFrom(localDate: LocalDate): AttributeValue {
    return AttributeValue.builder().s(localDate.toString()).build()
  }

  override fun type(): EnhancedType<LocalDate> {
    return EnhancedType.of(LocalDate::class.java)
  }

  override fun attributeValueType(): AttributeValueType {
    return AttributeValueType.S
  }
}
