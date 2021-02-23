package com.nicolasmilliard.socialcatsaws.imageprocessing.backend.functions

import com.nicolasmilliard.cloudmetric.CloudMetricModule
import com.nicolasmilliard.cloudmetric.CloudMetrics
import com.nicolasmilliard.di.scope.AppScope
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeComponent
import dagger.Module
import dagger.Provides
import software.amazon.awssdk.core.SdkSystemSetting
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import javax.inject.Singleton

@MergeComponent(AppScope::class)
@Singleton
interface AppComponent {
  fun getCloudMetrics(): CloudMetrics
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
  fun provideCloudMetrics(): CloudMetrics {
    return CloudMetricModule.provideCloudMetrics(System.getenv("APP_NAME"))
  }
}
