package com.nicolasmilliard.socialcats.featureflags

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RuntimeBehaviorTest {

    private val feature_defaultoff = TestFeatureFlag.TEST2
    private val feature_defaulton = TestFeatureFlag.TEST1

    @BeforeTest
    fun setUp() {
        RuntimeBehavior.clearFeatureFlagProviders()
    }

    @Test
    fun shouldReturnDefaultValueWhenNoProvider() {
        assertEquals(feature_defaultoff.defaultValue, RuntimeBehavior.isFeatureEnabled(feature_defaultoff))
    }

    @Test
    fun shouldGetValueFromProviderWhenAdded() {
        RuntimeBehavior.addProvider(TestProvider())
        assertTrue(RuntimeBehavior.isFeatureEnabled(feature_defaultoff))
    }

    @Test
    fun shouldGetDefaultValueWhenProviderDoesNotHaveValue() {
        RuntimeBehavior.addProvider(TestProvider())

        assertEquals(feature_defaulton.defaultValue, RuntimeBehavior.isFeatureEnabled(feature_defaulton))
    }

    @Test
    fun shouldGetDefaultValueFromHighestPriorityProvider() {
        RuntimeBehavior.addProvider(TestProvider())
        RuntimeBehavior.addProvider(MaxPriorityTestProvider())

        assertFalse(RuntimeBehavior.isFeatureEnabled(feature_defaultoff))
    }

//    @Test
//    fun shouldCallRefreshFeatureFlagWhenProviderHasARemoteFeatureFlagProvider() {
//        val provider = TestProvider()
//
//        RuntimeBehavior.addProvider(provider)
//
//        RuntimeBehavior.activateFeatureFlags()
//
//        assertTrue(provider.featureFlagsRefreshed)
//    }

    inner class TestProvider : FeatureFlagProvider, RemoteFeatureFlagProvider {

        var featureFlagsFetched: Boolean = false
        var featureFlagsActivated: Boolean = false

        override val priority = MIN_PRIORITY

        override fun isFeatureEnabled(feature: Feature): Boolean = true

        override fun hasFeature(feature: Feature): Boolean = feature == feature_defaultoff

        override suspend fun fetch(forceRefresh: Boolean) {
            featureFlagsFetched = true }

        override suspend fun activate() {
            featureFlagsActivated = true
        }
    }

    inner class MaxPriorityTestProvider : FeatureFlagProvider {

        override val priority = MAX_PRIORITY

        override fun isFeatureEnabled(feature: Feature): Boolean = false

        override fun hasFeature(feature: Feature): Boolean = true
    }

    enum class TestFeatureFlag(
        override val key: String,
        override val title: String,
        override val explanation: String,
        override val defaultValue: Boolean = true
    ) : Feature {
        TEST1("feature.test1", "Test Feature", "Enabled test feature"),
        TEST2("feature.test2", "Test Feature 2", "Enabled test feature", false)
    }
}
