package com.nicolasmilliard.socialcatsaws

import android.content.Context
import androidx.startup.AppInitializer
import androidx.work.WorkManager
import com.nicolasmilliard.activityresult.AppVersionCode
import com.nicolasmilliard.socialcatsaws.api.BackendApiUrl
import com.nicolasmilliard.socialcatsaws.api.BackendImageUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  fun provideAppInitializer(@ApplicationContext context: Context) =
    AppInitializer.getInstance(context)

  @Singleton
  @Provides
  fun provideApplicationScope(): CoroutineScope {
    return MainScope()
  }

  @Provides
  fun provideWorkManager(@ApplicationContext context: Context) = WorkManager.getInstance(context)

  @Singleton
  @Provides
  fun provideHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
      .addInterceptor(
        HttpLoggingInterceptor { message -> Timber.d(message) }.apply {
          level = BASIC
        }
      )
      .build()
  }

  @BackendApiUrl
  @Provides
  fun provideBackendApiUrl(@ApplicationContext context: Context) = context.getString(R.string.backend_api_url)

  @BackendImageUrl
  @Provides
  fun provideBackendImageUrl(@ApplicationContext context: Context) = context.getString(R.string.backend_image_url)

  @AppVersionCode
  @Provides
  fun provideAppVersionCode() = BuildConfig.VERSION_CODE
}
