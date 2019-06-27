package com.nicolasmilliard.socialcats.testsettings

import android.annotation.SuppressLint
import android.content.Context
import com.nicolasmilliard.socialcats.featureflags.Feature
import com.nicolasmilliard.socialcats.featureflags.FeatureFlagProvider

class RuntimeFeatureFlagProvider(priority: Int, appContext: Context) : FeatureFlagProvider {

    private val preferences = appContext.getSharedPreferences("runtime.featureflags", Context.MODE_PRIVATE)

    override val priority = priority

    override fun isFeatureEnabled(feature: Feature): Boolean =
            preferences.getBoolean(feature.key, feature.defaultValue)

    override fun hasFeature(feature: Feature): Boolean = true

    @SuppressLint("ApplySharedPref")
    fun setFeatureEnabled(feature: Feature, enabled: Boolean, commitNow: Boolean = false) {
        if (commitNow) {
            preferences.edit().putBoolean(feature.key, enabled).commit()
        } else {
            preferences.edit().putBoolean(feature.key, enabled).apply()
        }
    }
}
