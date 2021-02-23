package com.nicolasmilliard.socialcatsaws.profile.repository.dynamodb

import com.nicolasmilliard.cloudmetric.CloudMetrics
import com.nicolasmilliard.cloudmetric.Unit
import com.nicolasmilliard.socialcatsaws.profile.model.Device
import com.nicolasmilliard.socialcatsaws.profile.model.Image
import com.nicolasmilliard.socialcatsaws.profile.model.User
import com.nicolasmilliard.socialcatsaws.profile.model.UserWithImages
import com.nicolasmilliard.socialcatsaws.profile.repository.DbInvalidEntityException
import com.nicolasmilliard.socialcatsaws.profile.repository.InsertResult
import com.nicolasmilliard.socialcatsaws.profile.repository.UsersRepository
import mu.KotlinLogging
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.Put
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.QueryResponse
import software.amazon.awssdk.services.dynamodb.model.ReturnValue
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException
import software.amazon.awssdk.services.dynamodb.model.Update
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

public class UsersDynamoDb(
  private val client: DynamoDbClient,
  private val tableName: String,
  private val cloudMetrics: CloudMetrics
) :
  UsersRepository {
  private val enhancedClient = DynamoDbEnhancedClient.builder()
    .dynamoDbClient(client)
    .build()

  private val usersTableSchema = USERS_TABLE_SCHEMA
  private val usersTable = enhancedClient.table(tableName, usersTableSchema)

  private val imagesTableSchema = IMAGES_TABLE_SCHEMA
  private val imagesTable = enhancedClient.table(tableName, imagesTableSchema)

  private val devicesTableSchema = DEVICES_TABLE_SCHEMA
  private val devicesTable = enhancedClient.table(tableName, devicesTableSchema)

  override fun getUserAndImages(userId: String, newestImagesCount: Int): UserWithImages {
    logger.debug("getUserAndImages()")
    val userPK: AttributeValue = AttributeValue.builder()
      .s(UserItem.key(userId))
      .build()
    val queryRequest = QueryRequest.builder()
      .tableName(tableName)
      .keyConditionExpression("#pk = :pk")
      .expressionAttributeNames(
        mapOf(
          "#pk" to Schema.SharedAttributes.PARTITION_KEY,
        )
      )
      .expressionAttributeValues(
        mapOf(
          ":pk" to userPK,
        )
      )
      .scanIndexForward(true)
      .limit(1 + newestImagesCount)
      .build()

    val queryResponse: QueryResponse = client.query(queryRequest)
    val items: List<Map<String, AttributeValue>> = queryResponse.items()
    var user: User? = null
    val images = mutableListOf<Image>()
    items.forEach {
      val type = it[Schema.SharedAttributes.ITEM_TYPE]

      if (type != null && !type.s().isNullOrEmpty()) {
        when (type.s()) {
          Schema.UserItem.TYPE -> {
            if (user != null) throw DbInvalidEntityException("Several users returned")
            user = usersTableSchema.mapToItem(it).toUser()
          }
          Schema.ImageItem.TYPE -> images.add(imagesTableSchema.mapToItem(it).toImage())
        }
      }
    }
    return UserWithImages(user, images)
  }

  // fun insertUser(user: User): InsertResult {
  //     logger.debug("insertUser()")
  //     val item = user.toUserItem()
  //     val expression = Expression.builder()
  //         .expression("attribute_not_exists(${Schema.SharedAttributes.PARTITION_KEY})")
  //         .build()
  //     val putRequest = PutItemEnhancedRequest.builder(UserItem::class.java)
  //         .item(item)
  //         .conditionExpression(expression)
  //         .build()
  //     return try {
  //         userTable.putItem(putRequest)
  //         InsertResult.Added
  //     } catch (e: ConditionalCheckFailedException) {
  //         InsertResult.AlreadyExist
  //     }
  // }

  override fun updateUser(user: User): User {
    logger.debug("updateUser()")
    val updateItem = usersTable.updateItem(user.toUserItem())
    return updateItem.toUser()
  }

  override fun getUserById(id: String): User {
    logger.debug("getUserById()")
    val pk = UserItem.key(id)
    val key: Key = Key.builder().partitionValue(pk)
      .sortValue(pk).build()
    return usersTable.getItem(key).toUser()
  }

  override fun insertImage(image: Image): InsertResult {
    logger.debug("insertImage()")
    val timeValue = measureTimedValue {
      val imageItem = imagesTableSchema.itemToMap(image.toImageItem(), false)
      val imagePut = Put.builder()
        .tableName(tableName)
        .item(imageItem)
        .conditionExpression("attribute_not_exists(${Schema.SharedAttributes.PARTITION_KEY})")
        .build()
      val userPk = UserItem.key(image.userId)
      val addImageCount: Update = Update.builder()
        .tableName(tableName)
        .key(
          mapOf(
            Schema.SharedAttributes.PARTITION_KEY to AttributeValue.builder()
              .s(userPk)
              .build(),
            Schema.SharedAttributes.SORT_KEY to AttributeValue.builder()
              .s(userPk)
              .build()
          )
        )
        .updateExpression("SET ${Schema.UserItem.Attributes.IMAGE_COUNT} = if_not_exists(${Schema.UserItem.Attributes.IMAGE_COUNT}, :start) + :incr")
        .expressionAttributeValues(
          mapOf(
            ":start" to AttributeValue.builder().n("0").build(),
            ":incr" to AttributeValue.builder().n("1").build()
          )
        )
        .build()

      try {
        client.transactWriteItems(
          TransactWriteItemsRequest.builder()
            .transactItems(
              listOf(
                TransactWriteItem.builder()
                  .update(addImageCount)
                  .build(),
                TransactWriteItem.builder()
                  .put(imagePut)
                  .build()
              )
            )
            .build()
        )
        return@measureTimedValue InsertResult.Added
      } catch (e: TransactionCanceledException) {
        if (e.hasCancellationReasons()) {
          e.cancellationReasons().forEach {
            if (it.code() == "ConditionalCheckFailed") {
              return@measureTimedValue InsertResult.AlreadyExist
            }
          }
        }
        throw e
      }
    }

    cloudMetrics.putMetric(
      "DynamoInsertImage",
      timeValue.duration.inMilliseconds,
      Unit.MILLISECONDS
    )
    return timeValue.value
  }

  override fun countImages(userId: String): Int {
    logger.debug("countImages(): $userId")

    val userPk = UserItem.key(userId)
    val result = client.getItem(
      GetItemRequest.builder()
        .tableName(tableName)
        .key(
          mapOf(
            Schema.SharedAttributes.PARTITION_KEY to AttributeValue.builder()
              .s(userPk)
              .build(),
            Schema.SharedAttributes.SORT_KEY to AttributeValue.builder()
              .s(userPk).build()
          )
        )
        .projectionExpression(Schema.UserItem.Attributes.IMAGE_COUNT)
        .build()
    )
    logger.debug("countImages() ${result.item()}")
    logger.debug("countImages() ${result.item().keys}")
    return result.item().getValue(Schema.UserItem.Attributes.IMAGE_COUNT).n().toInt()
  }

  override fun insertDevice(device: Device): InsertResult {
    logger.debug("insertDevice()")
    try {
      devicesTable.putItem(
        PutItemEnhancedRequest.builder(DeviceItem::class.java)
          .item(device.toDeviceItem())
          .conditionExpression(
            Expression.builder()
              .expression("attribute_not_exists(#pk) and attribute_not_exists(#sk) and attribute_not_exists(#instanceId)")
              .expressionNames(mapOf(
                "#pk" to Schema.SharedAttributes.PARTITION_KEY,
                "#sk" to Schema.SharedAttributes.SORT_KEY,
                "#instanceId" to Schema.DeviceItem.Attributes.INSTANCE_ID
              ))
              .build()
          )
          .build()
      )
      return InsertResult.Added
    } catch (e: ConditionalCheckFailedException) {
      return InsertResult.AlreadyExist
    }
  }

  override fun updateNotificationKey(userId: String, notificationKey: String) {
    val userKey = UserItem.key(userId)
    try {
      client.updateItem(
        UpdateItemRequest.builder()
          .tableName(tableName)
          .key(
            mapOf(
              Schema.SharedAttributes.PARTITION_KEY to AttributeValue.builder()
                .s(userKey)
                .build(),
              Schema.SharedAttributes.SORT_KEY to AttributeValue.builder()
                .s(userKey)
                .build()
            )
          )
          .updateExpression("SET ${Schema.UserItem.Attributes.NOTIFICATION_KEY} = :value")
          .expressionAttributeValues(
            mapOf(
              ":value" to AttributeValue.builder().s(notificationKey).build()
            )
          )
          .conditionExpression("attribute_exists(${Schema.SharedAttributes.PARTITION_KEY}) and attribute_exists(${Schema.SharedAttributes.SORT_KEY})")
          .returnValues(ReturnValue.NONE)
          .build()
      )
    } catch (e: ConditionalCheckFailedException) {
      throw DbInvalidEntityException("No user found with id: $userId", e)
    }
  }

  override fun getTokens(userId: String): List<String> {
    // val userPK: AttributeValue = AttributeValue.builder()
    //   .s(UserItem.key(userId))
    //   .build()
    // val deviceSK: AttributeValue = AttributeValue.builder()
    //   .s(Schema.DeviceItem.KEY_PREFIX)
    //   .build()
    // client.query(QueryRequest.builder()
    //   .tableName(tableName)
    //   .keyConditionExpression("#pk = :pk and begins_with(#sk, :sk)")
    //   .expressionAttributeNames(
    //     mapOf(
    //       "#pk" to Schema.SharedAttributes.PARTITION_KEY,
    //       "#sk" to Schema.SharedAttributes.SORT_KEY,
    //     )
    //   )
    //   .expressionAttributeValues(
    //     mapOf(
    //       ":pk" to userPK,
    //       ":sk" to deviceSK,
    //     )
    //   )
    //   .scanIndexForward(true)
    //   .limit(50)
    //   .build())
    throw NotImplementedError()
  }
}
