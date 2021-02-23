package com.nicolasmilliard.socialcatsaws.imageupload

import androidx.work.WorkManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nicolasmilliard.socialcatsaws.api.BackendApiUrl
import com.nicolasmilliard.socialcatsaws.auth.Auth
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object ImageModule {

  @Singleton
  @Provides
  public fun provideImageUploadService(
    workManager: WorkManager,
    client: Lazy<OkHttpClient>,
    @BackendApiUrl backendApiUrl: String,
    auth: Auth,
    mainScope: CoroutineScope
  ): ImageUploadService {
    val json = Json
    val retrofit = Retrofit.Builder()
      .baseUrl(backendApiUrl)
      .callFactory { request -> client.get().newCall(request) }
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .build()
    val serviceApi: ImageUploadApi = retrofit.create()
    return ImageUploadService(workManager, serviceApi, json, auth, mainScope)
  }

  @Provides
  public fun provideImageUploadNav(): ImageUploadNav {
    return ImageUploadNav()
  }
}
