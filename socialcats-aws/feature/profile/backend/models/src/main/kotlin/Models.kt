package com.nicolasmilliard.socialcatsaws.profile.model

import kotlinx.datetime.Instant

public data class User(
  val id: String,
  var createdAt: Instant,
  val email: String,
  val emailVerified: Boolean,
  val name: String? = null,
  val avatar: Avatar? = null,
  val imageCount: Int = 0,
  val notificationKey: String? = null,
)

public data class Avatar(
  val imageId: String,
)

public data class Image(
  val id: String,
  val userId: String,
  var createdAt: Instant,
)

public data class UserWithImages(
  val user: User?,
  val images: List<Image>
)

public sealed class CreateSignedUrl {
  public data class CreateSignedUrlData(
    val url: String,
    val headers: Map<String, String>
  ) : CreateSignedUrl()
  public object MaxStoredImagesReached : CreateSignedUrl()
}

public enum class SupportedPlatform {
  ANDROID
}

public enum class DeviceIdProvider {
  FCM
}

public data class Device(
  val userId: String,
  val instanceId: String,
  var createdAt: Instant,
  val token: String,
  val provider: DeviceIdProvider,
  val platform: SupportedPlatform,
  val appVersionCode: Int,
  val languageTag: String
)
