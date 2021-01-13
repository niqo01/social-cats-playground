package com.nicolasmilliard.socialcatsaws.profile.repository.dynamodb

import com.nicolasmilliard.socialcatsaws.profile.model.Avatar
import com.nicolasmilliard.socialcatsaws.profile.model.Image
import com.nicolasmilliard.socialcatsaws.profile.model.User
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbFlatten
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

internal const val USER_TYPE = "user"
internal const val USER_RECORD = "A"

@DynamoDbBean
internal data class UserItem(
  @get:DynamoDbAttribute("user_Id")
  var id: String? = null,
  var email: String? = null,
  var email_verified: Boolean? = null,
  var name: String? = null,
  @get:DynamoDbFlatten
  var avatar: AvatarItem? = null,
  var image_count: Int? = null,
) {
  @get:DynamoDbPartitionKey
  var partition_key
    get() = prefixedId(id)
    set(_) {
      // Ignore needed by enhanced client
    }

  @get:DynamoDbSortKey
  var sort_key
    get() = USER_RECORD
    set(_) {
      // Ignore needed by enhanced client
    }

  var item_type
    get() = USER_TYPE
    set(_) {
      // Ignore needed by enhanced client
    }

  companion object {
    fun prefixedId(id: String?): String {
      return "$USER_TYPE#$id"
    }
  }
}

@DynamoDbBean
internal data class AvatarItem(
  var avatar_image_id: String? = null,
  var avatar_image_store_key: String? = null
)
internal fun AvatarItem.toAvatar() = Avatar(avatar_image_id!!, avatar_image_store_key!!)
internal fun Avatar.toAvatarItem() = AvatarItem(imageId, storeKey)

internal fun UserItem.toUser() = User(id!!, email!!, email_verified!!, name, avatar?.toAvatar(), image_count!!)
internal fun User.toUserItem() = UserItem(id, email, emailVerified, name, avatar?.toAvatarItem(), imageCount)

internal const val IMAGE_TYPE = "image"
internal const val IMAGE_KEY_PREFIX = "image#"

@DynamoDbBean
internal data class ImageItem(
  @get:DynamoDbAttribute("message_Id")
  var id: String? = null,
  var userId: String? = null,
  var storeKey: String? = null
) {
  @get:DynamoDbPartitionKey
  var partition_key
    get() = UserItem.prefixedId(userId)
    set(_) {
      // Ignore needed by enhanced client
    }

  @get:DynamoDbSortKey
  var sort_key
    get() = prefixedId(id)
    set(_) {
      // Ignore needed by enhanced client
    }

  var item_type
    get() = IMAGE_TYPE
    set(_) {
      // Ignore needed by enhanced client
    }

  companion object {
    fun prefixedId(id: String?): String {
      return "$IMAGE_KEY_PREFIX$id"
    }
  }
}

internal fun ImageItem.toImage() = Image(id!!, userId!!, storeKey!!)
internal fun Image.toImageItem() = ImageItem(id, userId, storeKey)
