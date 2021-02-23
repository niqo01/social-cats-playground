package com.nicolasmilliard.socialcatsaws.backend.repository.objectstore.s3

import com.nicolasmilliard.socialcatsaws.backend.repository.objectstore.ObjectStore
import com.nicolasmilliard.socialcatsaws.backend.repository.objectstore.PreSignedUrl
import mu.KotlinLogging
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

private val logger = KotlinLogging.logger {}

public class S3ObjectStore(
  private val s3Client: S3Client,
  private val bucketName: String
) : ObjectStore {

  override fun createPreSignedUrl(key: String, metadata: Map<String, String>): PreSignedUrl {
    return createS3PresignedUrl(bucketName, key, metadata)
  }

  override fun deleteFile(key: String) {
    s3Client.deleteObject {
      it.bucket(bucketName)
      it.key(key)
    }
  }

  private fun createS3PresignedUrl(
    bucketName: String,
    key: String,
    metadata: Map<String, String>
  ): PreSignedUrl {
    val request = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .metadata(metadata)
      .build()

    val putPresignRequest = PutObjectPresignRequest.builder()
      .signatureDuration(Duration.ofHours(24))
      .putObjectRequest(request)
      .build()

    // Generate the presigned request
    val presigner = S3Presigner.create()
    val presignedRequest = presigner.presignPutObject(putPresignRequest)
    presigner.close()

    logger.debug { "isBrowserExecutable: ${presignedRequest.isBrowserExecutable}}" }
    logger.debug { "signedHeaders: ${presignedRequest.signedHeaders()}}" }
    return PreSignedUrl(
      presignedRequest.url().toString(),
      presignedRequest.signedHeaders().mapValues { it.value.reduce { acc, s -> acc + s } }
    )
  }
}
