package com.nicolasmilliard.android.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class DataStoreRepository(private val store: DataStore<Preferences>) {

  public fun getString(key: String): Flow<String?> {
    val prefKey = stringPreferencesKey(key)
    return store.data.map { it[prefKey] }
  }

  public suspend fun writeString(key: String, value: String) {
    val prefKey = stringPreferencesKey(key)
    store.edit { settings ->
      settings[prefKey] = value
    }
  }
}
