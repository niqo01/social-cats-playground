package com.nicolasmilliard.socialcatsaws.profile.repository.dynamodb

import com.nicolasmilliard.cloudmetric.CloudMetrics
import com.nicolasmilliard.cloudmetric.Unit
import com.nicolasmilliard.socialcatsaws.profile.model.Image
import com.nicolasmilliard.socialcatsaws.profile.model.User
import com.nicolasmilliard.socialcatsaws.profile.model.UserWithImages
import com.nicolasmilliard.socialcatsaws.profile.repository.DbEntityAlreadyExistsException
import com.nicolasmilliard.socialcatsaws.profile.repository.DbInvalidEntityException
import com.nicolasmilliard.socialcatsaws.profile.repository.UsersRepository
import mu.KotlinLogging
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.Put
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.QueryResponse
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.ScanResponse
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest
import software.amazon.awssdk.services.dynamodb.model.Update
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

internal class UsersDynamoDb(
    private val client: DynamoDbClient,
    private val tableName: String,
    private val cloudMetrics: CloudMetrics
) :
    UsersRepository {
    private val enhancedClient = DynamoDbEnhancedClient.builder()
        .dynamoDbClient(client)
        .build()

    private val userTableSchema = TableSchema.fromClass(UserItem::class.java)
    private val userTable = enhancedClient.table(tableName, userTableSchema)

    private val imageTableSchema = TableSchema.fromClass(ImageItem::class.java)
    private val imageTable = enhancedClient.table(tableName, imageTableSchema)

    override fun getUserAndMessages(userId: String, newestImagesCount: Int): UserWithImages {
        logger.debug("getUserAndMessages()")
        val conversationPk: AttributeValue = AttributeValue.builder()
            .s(UserItem.prefixedId(userId))
            .build()
        val queryRequest = QueryRequest.builder()
            .tableName(tableName) // Define aliases for the Attribute, '#pk' and the value, ':pk'
            .keyConditionExpression("#pk = :pk") // '#pk' refers to the Attribute 'PK'
            .expressionAttributeNames(
                java.util.Map.of(
                    "#pk",
                    "partition_key"
                )
            ) // ':pk' refers to the customer PK of interest
            .expressionAttributeValues(
                java.util.Map.of(
                    ":pk",
                    conversationPk
                )
            ) // Search from "bottom to top"
            .scanIndexForward(false) // One customer, plus N newest orders
            .limit(1 + newestImagesCount)
            .build()

        // Use the DynamoDbClient directly rather than the
        // DynamoDbEnhancedClient or DynamoDbTable

        val queryResponse: QueryResponse = client.query(queryRequest)
        // The result is a list of items in a "DynamoDB JSON map"
        val items: List<Map<String, AttributeValue>> = queryResponse.items()
        var user: User? = null
        val images = mutableListOf<Image>()
        items.forEach {
            val type = it["item_type"]

            if (type == null || type.s().isNullOrEmpty()) {
                throw DbInvalidEntityException("Required attribute 'Type' is missing or empty on Item with attributes: $it")
            }
            when (type.s()) {
                USER_TYPE ->
                    user =
                        userTableSchema.mapToItem(it).toUser()
                IMAGE_TYPE -> images.add(imageTableSchema.mapToItem(it).toImage())
                else -> throw DbInvalidEntityException("Found unhandled Type=${type.s()} on Item with attributes: $it")
            }
        }
        if (user == null) {
            throw DbInvalidEntityException("No user found in query result")
        }
        return UserWithImages(user!!, images)
    }

    override fun insertUser(user: User) {
        logger.debug("insertUser()")
        val item = user.toUserItem()
        val expression = Expression.builder()
            .expression("attribute_not_exists(PK)")
            .build()
        val putRequest = PutItemEnhancedRequest.builder(UserItem::class.java)
            .item(item)
            .conditionExpression(expression)
            .build()
        try {
            userTable.putItem(putRequest)
        } catch (e: ConditionalCheckFailedException) {
            throw DbEntityAlreadyExistsException("Attempted to overwrite an item which already exists with PK=${item.partition_key}")
        }
    }

    override fun updateUser(user: User) {
        logger.debug("updateUser()")
        userTable.updateItem(user.toUserItem())
    }

    override fun getUserById(id: String): User {
        logger.debug("getUserById()")
        val key: Key = Key.builder().partitionValue(UserItem.prefixedId(id))
            .sortValue(USER_RECORD).build()
        return userTable.getItem(key).toUser()
    }

    override fun insertImage(image: Image) {
        logger.debug("insertImage()")
        val elasped = measureTime {
            val imageItem = imageTableSchema.itemToMap(image.toImageItem(), false)
            val imagePut = Put.builder()
                .tableName(tableName)
                .item(imageItem)
                .conditionExpression("attribute_not_exists(PK)")
                .build()
            val addImageCount: Update = Update.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        "partition_key" to AttributeValue.builder()
                            .s(UserItem.prefixedId(image.userId))
                            .build(),
                        "sort_key" to AttributeValue.builder()
                            .s(USER_RECORD)
                            .build()
                    )
                )
                .updateExpression("SET image_count = image_count + :incr")
                .expressionAttributeValues(
                    mapOf(
                        ":incr" to AttributeValue.builder().n("1").build()
                    )
                )
                .build()

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
        }
        cloudMetrics.putMetric("DynamoInsertImage", elasped.inMilliseconds, Unit.MILLISECONDS)
    }

    override fun countImages(userId: String): Int {
        logger.debug("countImages(): $userId")

        val result = client.getItem(
            GetItemRequest.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        "partition_key" to AttributeValue.builder().s(UserItem.prefixedId(userId))
                            .build(),
                        "sort_key" to AttributeValue.builder().s(USER_RECORD).build()
                    )
                )
                .projectionExpression("image_count")
                .build()
        )
        logger.debug("countImages() ${result.item()}")
        logger.debug("countImages() ${result.item().keys}")
        return result.item().getValue("image_count").n().toInt()
    }

    override fun deleteAllItems() {
        logger.warn("deleteAllItems()")
        val elasped = measureTime {
            val scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .attributesToGet("partition_key", "sort_key")
                .build()
            // Note: Normally, full-table scans should be avoided in DynamoDB
            val scanResponse: ScanResponse = client.scan(scanRequest)
            for (item in scanResponse.items()) {
                val deleteItemRequest = DeleteItemRequest.builder()
                    .tableName(tableName)
                    .key(
                        java.util.Map.of(
                            "partition_key",
                            item["partition_key"],
                            "sort_key",
                            item["sort_key"]
                        )
                    )
                    .build()
                client.deleteItem(deleteItemRequest)
            }
        }
        cloudMetrics.putMetric("DynamoDeleteAllItems", elasped.inMilliseconds, Unit.MILLISECONDS)
    }
}
