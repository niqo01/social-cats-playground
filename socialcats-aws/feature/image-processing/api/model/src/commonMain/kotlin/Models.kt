package com.nicolasmilliard.socialcatsaws.imageupload

import kotlinx.serialization.Serializable

@Serializable
public data class CreateUploadUrlResult(
  val preSignRequest: PreSignedRequest
)

@Serializable
public sealed class PreSignedRequest

@Serializable
public object MaxStoredImagesReached : PreSignedRequest()

@Serializable
public data class UploadData(
  val url: String,
  val headers: Map<String, String>
) : PreSignedRequest()
