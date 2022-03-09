@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.android.application)
//    alias(libs.plugins.spotify.ruler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.spotify.ruler)
}

group = "me.niqo"
version = "1.0"


dependencies {
    implementation(projects.common)
    implementation(libs.activity.compose)
}

android {
    val androidConfig: Map<String,Any> by extra
    buildToolsVersion = androidConfig["buildToolsVersion"] as String
    compileSdk = androidConfig["compileSdk"] as Int
    defaultConfig {

        applicationId = "me.niqo.android"
        minSdk = androidConfig["minSdk"] as Int
        targetSdk = androidConfig["targetSdk"] as Int
        versionCode = 1
        versionName = "1.0"
    }
    val javaOptions: Map<String,Any> by extra
    compileOptions {
        sourceCompatibility = javaOptions["sourceCompatibility"] as JavaVersion
        targetCompatibility = javaOptions["targetCompatibility"] as JavaVersion
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    kotlinOptions {
        val kotlinOptions: Map<String, String> by extra
        jvmTarget = kotlinOptions["jvmTarget"]!!
    }
}

ruler {
    abi.set("arm64-v8a")
    locale.set("en")
    screenDensity.set(480)
    sdkVersion.set(27)
}