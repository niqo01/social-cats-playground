package com.nicolasmilliard.socialcats.featureflags

import com.nicolasmilliard.socialcats.util.Lock
import com.nicolasmilliard.socialcats.util.withLock

/**
 * Check whether a feature should be enabled or not. Based on the priority of the different providers and if said
 * provider explicitly defines a value for that feature, the value of the flag is returned.
 */
object RuntimeBehavior {

    // TODO Use CopyOnWrite list when stately support collections in JS
    private val mutex = Lock()
    internal val providers = mutableListOf<FeatureFlagProvider>()

    fun isFeatureEnabled(feature: Feature): Boolean = mutex.withLock {
        return providers
            .filter { it.hasFeature(feature) }
            .minBy(FeatureFlagProvider::priority)
            ?.isFeatureEnabled(feature)
            ?: feature.defaultValue
    }

    suspend fun fetchFeatureFlags(forceRefresh: Boolean) {
        val remoteProviders = mutex.withLock {
            providers
                .filter { it is RemoteFeatureFlagProvider }.toList()
        }
        remoteProviders
                .forEach { (it as RemoteFeatureFlagProvider).fetch(forceRefresh) }
    }

    suspend fun activateFeatureFlags() = mutex.withLock {
        val remoteProviders = mutex.withLock {
            providers
                .filter { it is RemoteFeatureFlagProvider }.toList()
        }
        remoteProviders
            .filter { it is RemoteFeatureFlagProvider }
            .forEach { (it as RemoteFeatureFlagProvider).activate() }
    }

    fun addProvider(provider: FeatureFlagProvider): Unit = mutex.withLock {
        providers.add(provider)
    }

    fun clearFeatureFlagProviders() = mutex.withLock { providers.clear() }

    fun removeAllFeatureFlagProviders(priority: Int) = providers.removeAll(providers.filter { it.priority == priority })
}
