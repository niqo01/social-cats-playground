package com.nicolasmilliard.socialcatsaws.profile

import com.nicolasmilliard.cloudmetric.CloudMetrics
import com.nicolasmilliard.cloudmetric.Unit
import com.nicolasmilliard.repository.imageobjectstore.ImageObjectStore
import com.nicolasmilliard.repository.imageobjectstore.ImageStoreKey
import com.nicolasmilliard.socialcatsaws.profile.model.Avatar
import com.nicolasmilliard.socialcatsaws.profile.model.CreateSignedUrl
import com.nicolasmilliard.socialcatsaws.profile.model.Image
import com.nicolasmilliard.socialcatsaws.profile.repository.UsersRepository
import mu.KotlinLogging
import mu.withLoggingContext
import java.util.UUID

private val MEGABYTE = 1024L * 1024L
private val logger = KotlinLogging.logger {}

public class UploadImageUseCase(
  private val usersRepository: UsersRepository,
  private val imageObjectStore: ImageObjectStore,
  private val cloudMetrics: CloudMetrics
) {
  public fun createSignedUrl(userId: String): CreateSignedUrl {
    val imageCount = usersRepository.countImages(userId)
    if (imageCount >= 3) {
      logger.warn { "event=too_many_images_stored" }
      cloudMetrics.putMetric("TooManyImageStored", 1.0, Unit.COUNT)
      return CreateSignedUrl.MaxStoredImagesReached
    }
    val key = ImageStoreKey.create(userId)
    val preSignedUrl = imageObjectStore.createPreSignedUrl(key)
    logger.info { "event=image_upload_url_created" }
    cloudMetrics.putMetric("ImageUploadUrlCreatedCount", 1.0, Unit.COUNT)
    return CreateSignedUrl.CreateSignedUrlData(preSignedUrl.url, preSignedUrl.headers)
  }

  public fun onNewStoredImage(storeKey: String, size: Long) {
    val imageStoreKey = ImageStoreKey(storeKey)
    val userId = imageStoreKey.userId
    withLoggingContext("UserId" to userId) {
        if (size / MEGABYTE > 6) {
          logger.warn("event=image_stored_too_big")
          imageObjectStore.deleteImage(imageStoreKey)
          cloudMetrics.putMetric("ImageStoredTooBig", 1.0, Unit.COUNT)
        } else {
          val image = Image(id = UUID.randomUUID().toString(), userId = userId, storeKey = storeKey)
          usersRepository.insertImage(image)
          logger.info("event=repository_new_image")
          cloudMetrics.putMetric("ImageRepositoryCreated", 1.0, Unit.COUNT)
          updateAvatar(image)
        }
    }
  }

  private fun updateAvatar(image: Image) {
    val user = usersRepository.getUserById(image.userId)
    if (user.avatar?.imageId == image.id) {
      logger.error { IllegalStateException("Trying ot update and url to the same one") }
      return
    }
    usersRepository.updateUser(user.copy(avatar = Avatar(image.id, image.storeKey)))
    logger.info { "event=avatar_updated" }
  }
}
