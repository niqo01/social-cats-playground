package com.nicolasmilliard.android.datastore

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object PushNotificationModule {

  @Singleton
  @Provides
  public fun provideDataStoreRepository(@ApplicationContext context: Context): DataStoreRepository {
    val dataStore = PreferenceDataStoreFactory.create(
      produceFile = { context.preferencesDataStoreFile("settings") },
      migrations = listOf()
    )
    return DataStoreRepository(dataStore)
  }
}
