package com.nicolasmilliard.socialcats.featureflags

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

class FirebaseFeatureFlagProvider(priority: Int, isDevModeEnabled: Boolean) : FeatureFlagProvider,
    RemoteFeatureFlagProvider {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
        if (isDevModeEnabled) {
            configSettings.setMinimumFetchIntervalInSeconds(60)
        }
        remoteConfig.setConfigSettingsAsync(configSettings.build())
    }

    override val priority: Int = priority

    override fun isFeatureEnabled(feature: Feature): Boolean =
        remoteConfig.getBoolean(feature.key)

    override fun hasFeature(feature: Feature): Boolean {
        return false
//        return when (feature) {
//            FeatureFlag.DARK_MODE -> true
//            else -> false
//        }
    }

    override suspend fun fetch(forceRefresh: Boolean) {
        if (forceRefresh) {
            remoteConfig.fetch(0).await()
        } else {
            remoteConfig.fetch().await()
        }
    }

    override suspend fun activate() {
        remoteConfig.activate().await()
    }
}
