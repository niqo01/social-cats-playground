package com.nicolasmilliard.pushnotification

import androidx.work.WorkManager
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nicolasmilliard.activityresult.AppVersionCode
import com.nicolasmilliard.android.datastore.DataStoreRepository
import com.nicolasmilliard.socialcatsaws.api.BackendApiUrl
import com.nicolasmilliard.socialcatsaws.auth.Auth
import com.nicolasmilliard.socialcatsaws.pushnotification.SendRegTokenApi
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
public object PushNotificationModule {

  @Singleton
  @Provides
  public fun provideSendRegTokenService(
    @AppVersionCode appVersionCode: Int,
    workManager: WorkManager,
    client: Lazy<OkHttpClient>,
    @BackendApiUrl backendApiUrl: String,
    auth: Auth,
    mainScope: CoroutineScope,
    store: DataStoreRepository
  ): RegTokenService {
    val json = Json
    val retrofit = Retrofit.Builder()
      .baseUrl(backendApiUrl)
      .callFactory { request -> client.get().newCall(request) }
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .build()
    val serviceApi: SendRegTokenApi = retrofit.create()
    return RegTokenService(
      appVersionCode, workManager, serviceApi, auth,
      FirebaseInstallations.getInstance(), FirebaseMessaging.getInstance(), mainScope, store
    )
  }
}
