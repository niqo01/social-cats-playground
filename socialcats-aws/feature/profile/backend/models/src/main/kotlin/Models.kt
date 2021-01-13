package com.nicolasmilliard.socialcatsaws.profile.model

public data class User(
  val id: String,
  val email: String,
  val emailVerified: Boolean,
  val name: String? = null,
  val avatar: Avatar? = null,
  val imageCount: Int = 0,
)

public data class Avatar(
  val imageId: String,
  val storeKey: String,
)

public data class Image(
  val id: String,
  val userId: String,
  val storeKey: String,
)

public data class UserWithImages(
  val user: User,
  val images: List<Image>
)

public sealed class CreateSignedUrl {
  public data class CreateSignedUrlData(
    val url: String,
    val headers: Map<String, String>
  ) : CreateSignedUrl()
  public object MaxStoredImagesReached : CreateSignedUrl()
}
