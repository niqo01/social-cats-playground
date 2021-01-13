package com.nicolasmilliard.socialcatsaws.backend.repository.objectstore

public interface ObjectStore {
  public fun createPreSignedUrl(key: String, metadata: Map<String, String>): PreSignedUrl
  public fun deleteFile(key: String)
}

public data class PreSignedUrl(
  val url: String,
  val headers: Map<String, String>
)
