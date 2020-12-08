package com.nicolasmilliard.socialcats.testsettings

import com.nicolasmilliard.socialcats.featureflags.Feature
import com.nicolasmilliard.socialcats.featureflags.FeatureFlagProvider

/**
 * For use during unit/instrumentation tests, allows to dynamically enable/disable features
 * during automated tests
 */
class TestFeatureFlagProvider(priority: Int) : FeatureFlagProvider {

    private val features = HashMap<Feature, Boolean>()

    override val priority = priority

    override fun isFeatureEnabled(feature: Feature): Boolean = features[feature]!!

    override fun hasFeature(feature: Feature): Boolean = features.containsKey(feature)

    fun setFeatureEnabled(feature: Feature, enabled: Boolean) = features.put(feature, enabled)

    fun clearFeatures() = features.clear()
}
