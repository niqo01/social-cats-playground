package com.nicolasmilliard.socialcatsaws.profile.functions

import com.nicolasmilliard.cloudmetric.CloudMetricModule
import com.nicolasmilliard.cloudmetric.CloudMetrics
import com.nicolasmilliard.di.scope.AppScope
import com.nicolasmilliard.socialcatsaws.profile.NewUserUseCase
import com.nicolasmilliard.socialcatsaws.profile.repository.DynamoDbTableName
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeComponent
import dagger.Module
import dagger.Provides
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.core.SdkSystemSetting
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI
import javax.inject.Singleton

@MergeComponent(AppScope::class)
@Singleton
interface AppComponent {
  fun getCloudMetrics(): CloudMetrics
  fun getNewUserUseCase(): NewUserUseCase
}

@Module
@ContributesTo(AppScope::class)
object AppModule {

  @Singleton
  @Provides
  fun provideRegion(): String {
    return System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())
  }

  @Singleton
  @Provides
  fun provideSdkHttpClient(): SdkHttpClient {
    return UrlConnectionHttpClient.builder().build()
  }

  @Singleton
  @Provides
  fun provideDynamoDbClient(region: String, httpClient: SdkHttpClient): DynamoDbClient {
    return DynamoDbClient.builder()
      .httpClient(httpClient)
      .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
      .region(Region.of(region))
      .overrideConfiguration(ClientOverrideConfiguration.builder().build())
      .endpointOverride(URI("https://dynamodb.$region.amazonaws.com"))
      .build()
  }

  @DynamoDbTableName
  @Provides
  fun provideDynamoDbTableName(): String {
    return System.getenv("DDB_TABLE_NAME")
  }

  @Singleton
  @Provides
  fun provideCloudMetrics(): CloudMetrics {
    return CloudMetricModule.provideCloudMetrics(System.getenv("APP_NAME"))
  }
}
