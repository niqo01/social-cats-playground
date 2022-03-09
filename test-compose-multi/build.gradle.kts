@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.bencheck)
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.qodana)
}

group = "me.niqo"
version = "1.0"

subprojects {
    extra["androidConfig"] = mapOf(
        "buildToolsVersion" to "31.0.0",
        "compileSdk" to 31,
        "minSdk" to 26,
        "targetSdk" to 31,
    )
    extra["kotlinOptions"] = mapOf(
        "jvmTarget" to "11",
    )
    extra["javaOptions"] = mapOf(
        "sourceCompatibility" to JavaVersion.VERSION_11,
        "targetCompatibility" to JavaVersion.VERSION_11,
    )
}

qodana {
    saveReport.set(true)
}