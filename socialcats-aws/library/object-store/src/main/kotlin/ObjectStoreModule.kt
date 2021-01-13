package com.nicolasmilliard.socialcatsaws.backend.repository.objectstore

import com.nicolasmilliard.socialcatsaws.backend.repository.objectstore.s3.S3ObjectStore
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

public object ObjectStoreModule {
  public fun provideObjectStore(bucketName: String, region: String): ObjectStore {
    val httpClient = UrlConnectionHttpClient.builder().build()
    val s3Client = S3Client.builder()
      .httpClient(httpClient)
      .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
      .region(Region.of(region))
      .overrideConfiguration(ClientOverrideConfiguration.builder().build())
      .endpointOverride(URI("https://s3.$region.amazonaws.com"))
      .build()
    return S3ObjectStore(s3Client, bucketName)
  }
}
