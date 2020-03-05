package com.nicolasmilliard.socialcats.featureflags

import kotlin.test.Test
import kotlin.test.assertEquals

class StoreFeatureFlagProviderTest {
    @Test
    fun hasLowestPriority() {
        assertEquals(MIN_PRIORITY, StoreFeatureFlagProvider().priority)
    }

//    @Test
//    fun hasValueForEveryToggle() {
//        assertTrue(StoreFeatureFlagProvider().hasFeature())
//    }
//
//    @Test
//    fun darkModeOffByDefault() {
//        assertFalse(StoreFeatureFlagProvider().isFeatureEnabled(FeatureFlag.DARK_MODE))
//    }

    // more tests here
}
