package com.nicolasmilliard.repository.imageobjectstore

import com.nicolasmilliard.socialcatsaws.backend.repository.objectstore.ObjectStore
import com.nicolasmilliard.socialcatsaws.backend.repository.objectstore.PreSignedUrl
import java.util.UUID
import javax.inject.Inject

public class ImageObjectStore @Inject constructor(private val objectStore: ObjectStore) {

  public fun createPreSignedUrl(imageKey: ImageStoreKey): PreSignedImageUrl {
    val metadata = mapOf("user-id" to imageKey.userId)
    return objectStore.createPreSignedUrl(imageKey.storeKey, metadata).toPreSignedImageUrl()
  }

  public fun deleteImage(imageKey: ImageStoreKey) {
    objectStore.deleteFile(imageKey.storeKey)
  }
}

public data class ImageStoreKey(val storeKey: String) {
  val userId: String by lazy {
    storeKey.split("/")[1]
  }

  public companion object {
    public fun create(userId: String): ImageStoreKey {
      return ImageStoreKey("u/$userId/${UUID.randomUUID()}")
    }
  }
}

public data class PreSignedImageUrl(
  val url: String,
  val headers: Map<String, String>
)

private fun PreSignedUrl.toPreSignedImageUrl() = PreSignedImageUrl(url, headers)
