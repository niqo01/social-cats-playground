package com.nicolasmilliard.socialcats.featureflags

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoreFeatureFlagProviderTest {
    @Test
    fun hasLowestPriority() {
        assertEquals(MIN_PRIORITY, StoreFeatureFlagProvider().priority)
    }

    @Test
    fun hasValueForEveryToggle() {
        assertTrue(StoreFeatureFlagProvider().hasFeature(FeatureFlag.DARK_MODE))
    }

    @Test
    fun darkModeOffByDefault() {
        assertFalse(StoreFeatureFlagProvider().isFeatureEnabled(FeatureFlag.DARK_MODE))
    }

    // more tests here
}
