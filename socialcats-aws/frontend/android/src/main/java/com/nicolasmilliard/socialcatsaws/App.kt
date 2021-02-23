package com.nicolasmilliard.socialcatsaws

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.AppInitializer
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import com.nicolasmilliard.pushnotification.RegTokenService
import com.nicolasmilliard.socialcatsaws.api.BackendImageUrl
import com.nicolasmilliard.socialcatsaws.imageupload.SharpInterceptor
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider, ImageLoaderFactory {

  @Inject
  lateinit var appInitializer: AppInitializer

  @Inject
  lateinit var workerFactory: HiltWorkerFactory

  @Inject
  lateinit var okHttp: OkHttpClient

  @Inject
  @BackendImageUrl
  lateinit var backendImageUrl: String

  @Inject
  lateinit var mainScope: CoroutineScope

  @Inject
  lateinit var regTokenService: RegTokenService

  override fun onCreate() {
    super.onCreate()
    Timber.plant(DebugTree())
    appInitializer.initializeComponent(AmplifyInitializer::class.java)

    regTokenService.init()
  }

  override fun getWorkManagerConfiguration() =
    Configuration.Builder()
      .setMinimumLoggingLevel(android.util.Log.INFO)
      .setWorkerFactory(workerFactory)
      .build()

  override fun newImageLoader(): ImageLoader {
    return ImageLoader.Builder(applicationContext)
      .crossfade(true)
      .okHttpClient {
        okHttp.newBuilder()
          .cache(CoilUtils.createDefaultCache(applicationContext))
          .build()
      }
      .componentRegistry {
        add(SharpInterceptor(backendImageUrl))
      }
      .build()
  }
}
