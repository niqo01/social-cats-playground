package com.nicolasmilliard.socialcats.testsettings

import com.nicolasmilliard.socialcats.featureflags.Feature

/**
 * A test setting is something that stays in our app forever (hence it is a tool to simplify testing)
 * e.g. it is a hook into our app to allow something that a production app shouldn’t allow. (enable logging, bypass software update,…)
 *
 * Test settings must never be exposed via our remote feature flag tool.
 */
enum class TestSetting(
    override val key: String,
    override val title: String,
    override val explanation: String,
    override val defaultValue: Boolean = false
) : Feature {
    USE_DEVELOP_PORTAL("testsetting.usedevelopportal", "Development portal", "Use developer REST endpoint", true),
    IDLING_RESOURCES("testsetting.idlingresources", "Idling resources", "Enable idling resources for Espresso"),
    LEAK_CANARY("testsetting.leakcanary", "Leak Canary", "Enable leak canary", true),
    STRICT_MODE(
        "testsetting.strictmode",
        "Enable strict mode",
        "Detect IO operations accidentally performed on the main Thread",
        defaultValue = true
    ),
    CRASH_APP("testsetting.crashapp", "Crash app", "Force java crash next app startup"),
    DEBUG_FIREBASE(
        "testsetting.debugfirebase",
        "Enable Firebase remote config (DEBUG Builds)",
        "Enable reading feature flag from Firebase on debug builds",
        true
    )
}
