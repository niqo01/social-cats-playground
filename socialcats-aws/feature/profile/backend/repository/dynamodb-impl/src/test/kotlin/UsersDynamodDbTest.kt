package com.nicolasmilliard.socialcatsaws.profile.repository.dynamodb

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer
import com.nicolasmilliard.cloudmetric.FakeCloudMetrics
import com.nicolasmilliard.socialcatsaws.profile.model.Avatar
import com.nicolasmilliard.socialcatsaws.profile.model.Device
import com.nicolasmilliard.socialcatsaws.profile.model.DeviceIdProvider
import com.nicolasmilliard.socialcatsaws.profile.model.Image
import com.nicolasmilliard.socialcatsaws.profile.model.SupportedPlatform
import com.nicolasmilliard.socialcatsaws.profile.model.User
import com.nicolasmilliard.socialcatsaws.profile.repository.InsertResult
import kotlinx.datetime.Clock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.core.waiters.WaiterResponse
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BillingMode
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter
import java.net.URI

internal class UsersDynamodDbTest {
  lateinit var sClient: DynamoDbClient
  lateinit var sServer: DynamoDBProxyServer

  @BeforeEach
  fun beforeEach() {
    System.setProperty("sqlite4java.library.path", "./build/libs/")

    // Create an in-memory and in-process instance of DynamoDB Local that runs over HTTP
    val localArgs = arrayOf("-inMemory")

    try {
      sServer = ServerRunner.createServerFromCommandLineArgs(localArgs)
      sServer.safeStart()
    } catch (e: Exception) {
      fail(e.message)
    }
    createAmazonDynamoDBClient()
    createMyTables()

    val scanResultBefore = sClient.scan { it.tableName(Schema.TABLE_NAME).build() }
    assertEquals(0, scanResultBefore.count())
  }

  @AfterEach
  fun afterEach() {
    // deleteTables()
    try {
      sServer.stop()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun createAmazonDynamoDBClient() {
    sClient = DynamoDbClient.builder()
      .httpClient(UrlConnectionHttpClient.builder().build())
      .region(Region.of("us-west-2"))
      .endpointOverride(URI("http://localhost:8000"))
      .credentialsProvider {
        object : AwsCredentials {
          override fun accessKeyId() = "ACCESS-KEY"
          override fun secretAccessKey() = "SECRET_KEY"
        }
      }
      .build()
  }

  private fun deleteTables() {
    val dbWaiter: DynamoDbWaiter = sClient.waiter()
    val request = DeleteTableRequest.builder()
      .tableName(Schema.TABLE_NAME)
      .build()
    val response = sClient.deleteTable(request)
    val waiterResponse: WaiterResponse<DescribeTableResponse> =
      dbWaiter.waitUntilTableNotExists(DescribeTableRequest.builder().tableName(Schema.TABLE_NAME).build())
    waiterResponse.matched().response().ifPresent(System.out::println)
    val deletedTable = response.tableDescription().tableName()
    println("Deleted $deletedTable")
  }
  private fun createMyTables() {
    val dbWaiter: DynamoDbWaiter = sClient.waiter()
    val request: CreateTableRequest = CreateTableRequest.builder()
      .attributeDefinitions(
        AttributeDefinition.builder()
          .attributeName(Schema.SharedAttributes.PARTITION_KEY)
          .attributeType(ScalarAttributeType.S)
          .build(),
        AttributeDefinition.builder()
          .attributeName(Schema.SharedAttributes.SORT_KEY)
          .attributeType(ScalarAttributeType.S)
          .build(),
      )
      .keySchema(
        KeySchemaElement.builder()
          .attributeName(Schema.SharedAttributes.PARTITION_KEY)
          .keyType(KeyType.HASH)
          .build(),
        KeySchemaElement.builder()
          .attributeName(Schema.SharedAttributes.SORT_KEY)
          .keyType(KeyType.RANGE)
          .build(),
      )
      .billingMode(BillingMode.PAY_PER_REQUEST)
      .tableName(Schema.TABLE_NAME)
      .build()

    try {
      val response: CreateTableResponse = sClient.createTable(request)
      val tableRequest: DescribeTableRequest = DescribeTableRequest.builder()
        .tableName(Schema.TABLE_NAME)
        .build()

      // Wait until the Amazon DynamoDB table is created
      val waiterResponse: WaiterResponse<DescribeTableResponse> =
        dbWaiter.waitUntilTableExists(tableRequest)
      waiterResponse.matched().response().ifPresent(System.out::println)
      val newTable = response.tableDescription().tableName()
      println("Created $newTable")
    } catch (e: DynamoDbException) {
      fail(e)
    }
  }

  @Test
  fun testUpdateUser() {

    val usersDynamoDb = UsersDynamoDb(sClient, Schema.TABLE_NAME, FakeCloudMetrics())
    val user = User("id", Clock.System.now(), "email", true, "Name", Avatar("imageId"), 0)
    usersDynamoDb.updateUser(user)

    val scanResult = sClient.scan { it.tableName(Schema.TABLE_NAME).build() }
    assertEquals(1, scanResult.count())
    val items = scanResult.items()
    checkUserItem(user, items[0])
  }

  @Test
  fun testInsertUserAlreadyExist() {

    val usersDynamoDb = UsersDynamoDb(sClient, Schema.TABLE_NAME, FakeCloudMetrics())
    val user = User("id", Clock.System.now(), "email", true, "Name", Avatar("imageId"), 0)
    usersDynamoDb.updateUser(user)
    usersDynamoDb.updateUser(user)

    val scanResult = sClient.scan { it.tableName(Schema.TABLE_NAME).build() }
    assertEquals(1, scanResult.count())
    val items = scanResult.items()
    checkUserItem(user, items[0])
  }

  @Test
  fun testInsertImage() {

    val usersDynamoDb = UsersDynamoDb(sClient, Schema.TABLE_NAME, FakeCloudMetrics())
    val user = User("id", Clock.System.now(), "email", true, "Name", Avatar("imageId"), 0)
    usersDynamoDb.updateUser(user)

    val image = Image("id", "id", Clock.System.now())
    usersDynamoDb.insertImage(image)

    val scanResult = sClient.scan { it.tableName(Schema.TABLE_NAME).build() }
    assertEquals(2, scanResult.count())
    val item = scanResult.items()[1]
    assertEquals(1, item.getValue(Schema.UserItem.Attributes.IMAGE_COUNT).n().toInt())

    val imageItem = scanResult.items()[0]
    assertEquals(Schema.UserItem.KEY_PREFIX + image.userId, imageItem.getValue(Schema.SharedAttributes.PARTITION_KEY).s())
    assertEquals("${Schema.ImageItem.KEY_PREFIX}${image.createdAt}#${image.id}", imageItem.getValue(Schema.SharedAttributes.SORT_KEY).s())
    assertEquals(image.id, imageItem.getValue(Schema.ImageItem.Attributes.MESSAGE_ID).s())
    assertEquals(image.userId, imageItem.getValue(Schema.ImageItem.Attributes.USER_ID).s())
  }

  @Test
  fun testInsertImageNoPriorUser() {

    val usersDynamoDb = UsersDynamoDb(sClient, Schema.TABLE_NAME, FakeCloudMetrics())

    val image = Image("id", "id", Clock.System.now())
    usersDynamoDb.insertImage(image)

    val scanResult = sClient.scan { it.tableName(Schema.TABLE_NAME).build() }
    assertEquals(2, scanResult.count())
    val item = scanResult.items()[1]
    assertEquals(Schema.UserItem.KEY_PREFIX + image.userId, item.getValue(Schema.SharedAttributes.PARTITION_KEY).s())
    assertEquals(Schema.UserItem.KEY_PREFIX + image.userId, item.getValue(Schema.SharedAttributes.SORT_KEY).s())
    assertEquals(1, item.getValue(Schema.UserItem.Attributes.IMAGE_COUNT).n().toInt())

    val imageItem = scanResult.items()[0]
    assertEquals(image.id, imageItem.getValue(Schema.ImageItem.Attributes.MESSAGE_ID).s())
    assertEquals(image.userId, imageItem.getValue(Schema.ImageItem.Attributes.USER_ID).s())
  }

  @Test
  fun testInsertImageAlreadyExisting() {

    val usersDynamoDb = UsersDynamoDb(sClient, Schema.TABLE_NAME, FakeCloudMetrics())

    val image = Image("id", "id", Clock.System.now())
    val insertImage = usersDynamoDb.insertImage(image)
    assertEquals(InsertResult.Added, insertImage)

    val insertImage1 = usersDynamoDb.insertImage(image)
    assertEquals(InsertResult.AlreadyExist, insertImage1)

    val scanResult = sClient.scan { it.tableName(Schema.TABLE_NAME).build() }
    assertEquals(2, scanResult.count())
    val item = scanResult.items()[1]
    assertEquals(Schema.UserItem.KEY_PREFIX + image.userId, item.getValue(Schema.SharedAttributes.PARTITION_KEY).s())
    assertEquals(Schema.UserItem.KEY_PREFIX + image.userId, item.getValue(Schema.SharedAttributes.SORT_KEY).s())
    assertEquals(1, item.getValue(Schema.UserItem.Attributes.IMAGE_COUNT).n().toInt())

    val imageItem = scanResult.items()[0]
    assertEquals(image.id, imageItem.getValue(Schema.ImageItem.Attributes.MESSAGE_ID).s())
    assertEquals(image.userId, imageItem.getValue(Schema.ImageItem.Attributes.USER_ID).s())
  }

  @Test
  fun testInsertImageAndThenUpdateUser() {

    val usersDynamoDb = UsersDynamoDb(sClient, Schema.TABLE_NAME, FakeCloudMetrics())

    val image = Image("id", "id", Clock.System.now())
    usersDynamoDb.insertImage(image)

    val user = User("id", Clock.System.now(), "email", true, "Name", Avatar("imageId"), 0)
    usersDynamoDb.updateUser(user)

    val scanResult = sClient.scan { it.tableName(Schema.TABLE_NAME).build() }
    assertEquals(2, scanResult.count())
    val item = scanResult.items()[1]
    assertEquals(Schema.UserItem.KEY_PREFIX + user.id, item.getValue(Schema.SharedAttributes.PARTITION_KEY).s())
    assertEquals(Schema.UserItem.KEY_PREFIX + image.userId, item.getValue(Schema.SharedAttributes.SORT_KEY).s())
    assertEquals(Schema.UserItem.TYPE, item.getValue(Schema.SharedAttributes.ITEM_TYPE).s())
    assertEquals(user.id, item.getValue(Schema.UserItem.Attributes.USER_ID).s())
    assertEquals(user.email, item.getValue(Schema.UserItem.Attributes.EMAIL).s())
    assertEquals(user.emailVerified, item.getValue(Schema.UserItem.Attributes.EMAIL_VERIFIED).bool())
    assertEquals(user.name, item.getValue(Schema.UserItem.Attributes.NAME).s())
    assertEquals(user.avatar!!.imageId, item.getValue(Schema.UserItem.Attributes.AVATAR_IMAGE_ID).s())
    assertEquals(1, item.getValue(Schema.UserItem.Attributes.IMAGE_COUNT).n().toInt())

    val imageItem = scanResult.items()[0]
    assertEquals(image.id, imageItem.getValue(Schema.ImageItem.Attributes.MESSAGE_ID).s())
    assertEquals(image.userId, imageItem.getValue(Schema.ImageItem.Attributes.USER_ID).s())
  }

  @Test
  fun testGetUserWithImages() {
    val usersDynamoDb = UsersDynamoDb(sClient, Schema.TABLE_NAME, FakeCloudMetrics())

    for (i in 1..3) {
      val userId = "userId$i"
      val user = User(userId, Clock.System.now(), "email", true, "Name", Avatar("imageId"), 0)
      usersDynamoDb.updateUser(user)

      for (i in 1..5) {
        val image = Image("id$i", userId, Clock.System.now())
        usersDynamoDb.insertImage(image)
      }
    }

    val userAndImages = usersDynamoDb.getUserAndImages("userId2", 3)
    assertNotNull(userAndImages.user)
    assertEquals(5, userAndImages.user!!.imageCount)
    assertEquals(3, userAndImages.images.size)
  }

  @Test
  fun testAddDevice() {

    val usersDynamoDb = UsersDynamoDb(sClient, Schema.TABLE_NAME, FakeCloudMetrics())
    val user = User("id", Clock.System.now(), "email", true, "Name", Avatar("imageId"), 0)
    usersDynamoDb.updateUser(user)

    val device = Device(
      user.id,
      "InstanceId",
      Clock.System.now(),
      "token",
      DeviceIdProvider.FCM,
      SupportedPlatform.ANDROID,
      12,
      "fr"
    )

    val result = usersDynamoDb.insertDevice(device)
    assertEquals(InsertResult.Added, result)
    val scanResult = sClient.scan { it.tableName(Schema.TABLE_NAME).build() }

    assertEquals(2, scanResult.count())
    val items = scanResult.items()
    checkUserItem(user, items[1])
    checkDeviceItem(device, items[0])
  }

  @Test
  fun testAddDeviceAlreadyExist() {

    val usersDynamoDb = UsersDynamoDb(sClient, Schema.TABLE_NAME, FakeCloudMetrics())
    val user = User("id", Clock.System.now(), "email", true, "Name", Avatar("imageId"), 0)
    usersDynamoDb.updateUser(user)

    val device = Device(
      user.id,
      "InstanceId",
      Clock.System.now(),
      "token",
      DeviceIdProvider.FCM,
      SupportedPlatform.ANDROID,
      12,
      "fr"
    )
    val result = usersDynamoDb.insertDevice(device)
    assertEquals(InsertResult.Added, result)
    val result2 = usersDynamoDb.insertDevice(device)
    assertEquals(InsertResult.AlreadyExist, result2)

    val scanResult = sClient.scan { it.tableName(Schema.TABLE_NAME).build() }
    assertEquals(2, scanResult.count())
    val items = scanResult.items()
    checkUserItem(user, items[1])
    checkDeviceItem(device, items[0])
  }

  @Test
  fun testUpdateNotificationKey() {

    val usersDynamoDb = UsersDynamoDb(sClient, Schema.TABLE_NAME, FakeCloudMetrics())
    val user = User("id", Clock.System.now(), "email", true, "Name", Avatar("imageId"), 0, null)
    usersDynamoDb.updateUser(user)

    val notifKey = "NotificationKey"
    usersDynamoDb.updateNotificationKey(user.id, notifKey)

    val scanResult = sClient.scan { it.tableName(Schema.TABLE_NAME).build() }
    assertEquals(1, scanResult.count())
    val items = scanResult.items()
    checkUserItem(user.copy(notificationKey = notifKey), items[0])
  }

  private fun checkUserItem(user: User, item: Map<String, AttributeValue>) {
    assertEquals(Schema.UserItem.KEY_PREFIX + user.id, item.getValue(Schema.SharedAttributes.PARTITION_KEY).s())
    assertEquals(Schema.UserItem.KEY_PREFIX + user.id, item.getValue(Schema.SharedAttributes.SORT_KEY).s())
    assertEquals(Schema.UserItem.TYPE, item.getValue(Schema.SharedAttributes.ITEM_TYPE).s())
    assertEquals(user.id, item.getValue(Schema.UserItem.Attributes.USER_ID).s())
    assertEquals(user.createdAt.toString(), item.getValue(Schema.UserItem.Attributes.CREATED_AT).s())
    assertEquals(user.email, item.getValue(Schema.UserItem.Attributes.EMAIL).s())
    assertEquals(user.emailVerified, item.getValue(Schema.UserItem.Attributes.EMAIL_VERIFIED).bool())

    if (user.name == null) {
      assertFalse(item.containsKey(Schema.UserItem.Attributes.NAME))
    } else {
      assertEquals(user.name, item.getValue(Schema.UserItem.Attributes.NAME).s())
    }
    if (user.avatar == null) {
      assertFalse(item.containsKey(Schema.UserItem.Attributes.AVATAR_IMAGE_ID))
    } else {
      assertEquals(user.avatar!!.imageId, item.getValue(Schema.UserItem.Attributes.AVATAR_IMAGE_ID).s())
    }

    assertEquals(user.imageCount, item.getValue(Schema.UserItem.Attributes.IMAGE_COUNT).n().toInt())
    if (user.notificationKey == null) {
      assertFalse(item.containsKey(Schema.UserItem.Attributes.NOTIFICATION_KEY))
    } else {
      assertEquals(user.notificationKey, item.getValue(Schema.UserItem.Attributes.NOTIFICATION_KEY).s())
    }
  }

  private fun checkDeviceItem(device: Device, item: Map<String, AttributeValue>) {
    assertEquals(Schema.UserItem.KEY_PREFIX + device.userId, item.getValue(Schema.SharedAttributes.PARTITION_KEY).s())
    assertEquals("${Schema.DeviceItem.KEY_PREFIX}${device.instanceId}", item.getValue(Schema.SharedAttributes.SORT_KEY).s())
    assertEquals(device.instanceId, item.getValue(Schema.DeviceItem.Attributes.INSTANCE_ID).s())
    assertEquals(device.createdAt.toString(), item.getValue(Schema.DeviceItem.Attributes.CREATED_AT).s())
    assertEquals(device.token, item.getValue(Schema.DeviceItem.Attributes.TOKEN).s())
    assertEquals(device.platform.toString(), item.getValue(Schema.DeviceItem.Attributes.PLATFORM).s())
    assertEquals(device.provider.toString(), item.getValue(Schema.DeviceItem.Attributes.PROVIDER).s())
    assertEquals(device.languageTag, item.getValue(Schema.DeviceItem.Attributes.LANGUAGE_TAG).s())
    assertEquals(device.appVersionCode, item.getValue(Schema.DeviceItem.Attributes.APP_VERSION_CODE).n().toInt())
  }
}
