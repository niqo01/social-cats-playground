package com.nicolasmilliard.socialcats.featureflags

class StoreFeatureFlagProvider(priority: Int) : FeatureFlagProvider {

    override val priority = priority

    @Suppress("ComplexMethod")
    override fun isFeatureEnabled(feature: Feature): Boolean {
        return false
//        return if (feature is FeatureFlag) {
//            // No "else" branch here -> choosing the default option for release must be an explicit choice
//            when (feature) {
//                // Add feature here
//                DARK_MODE -> false
//            }
//            false
//        } else {
//            // TestSettings should never be shipped to users
//            false
//        }
    }

    override fun hasFeature(feature: Feature): Boolean = true
}
