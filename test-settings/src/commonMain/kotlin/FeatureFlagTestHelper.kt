package com.nicolasmilliard.socialcats.testsettings

import com.nicolasmilliard.socialcats.featureflags.Feature
import com.nicolasmilliard.socialcats.featureflags.FeatureFlagProvider
import com.nicolasmilliard.socialcats.featureflags.MAX_PRIORITY
import com.nicolasmilliard.socialcats.featureflags.RuntimeBehavior

/**
 * Must be used only from test source code.
 */
class FeatureFlagTestHelper {

    companion object {
        @JvmStatic
        fun enableFeatureFlag(feature: Feature) {
            RuntimeBehavior.addProvider(TestFeatureFlagProvider(feature))
        }

        @JvmStatic
        fun disableFeatureFlag(feature: Feature) {
            RuntimeBehavior.addProvider(TestFeatureFlagProvider(feature, false))
        }

        @JvmStatic
        fun clearFeatureFlags() {
            RuntimeBehavior.removeAllFeatureFlagProviders(TEST_PRIORITY)
        }

        const val TEST_PRIORITY = MAX_PRIORITY - 1 // preceeds everyone
    }

    private class TestFeatureFlagProvider(val feature: Feature, val enabled: Boolean = true) : FeatureFlagProvider {
        override val priority: Int = TEST_PRIORITY

        override fun isFeatureEnabled(feature: Feature) = if (feature == this.feature) enabled else false

        override fun hasFeature(feature: Feature) = (feature == this.feature)
    }
}
