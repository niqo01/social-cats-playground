package com.nicolasmilliard.socialcatsaws.profile.repository

import com.nicolasmilliard.cloudmetric.CloudMetrics
import com.nicolasmilliard.socialcatsaws.profile.repository.dynamodb.UsersDynamoDb
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI

public object UsersRepositoryModule {

  public fun provideSocialCatsRepository(tableName: String, cloudMetrics: CloudMetrics, region: String): UsersRepository {
    val httpClient = UrlConnectionHttpClient.builder().build()
    val dynamoClient = DynamoDbClient.builder()
      .httpClient(httpClient)
      .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
      .region(Region.of(region))
      .overrideConfiguration(ClientOverrideConfiguration.builder().build())
      .endpointOverride(URI("https://dynamodb.$region.amazonaws.com"))
      .build()
    return UsersDynamoDb(dynamoClient, tableName, cloudMetrics)
  }
}
