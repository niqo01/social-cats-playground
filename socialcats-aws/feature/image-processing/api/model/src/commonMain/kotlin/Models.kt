package com.nicolasmilliard.socialcatsaws.imageupload

import kotlinx.serialization.Serializable

@Serializable
data class CreateUploadUrlResult(
  val preSignRequest: PreSignedRequest
)

@Serializable
sealed class PreSignedRequest

@Serializable
object MaxStoredImagesReached : PreSignedRequest()

@Serializable
data class UploadData(
  val url: String,
  val headers: Map<String, String>
) : PreSignedRequest()
