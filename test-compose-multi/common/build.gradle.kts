import org.jetbrains.compose.compose

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.android.library)
}

group = "me.niqo"
version = "1.0"

kotlin {
    android()
    jvm("desktop") {
        val kotlinOptions: Map<String,Any> by extra
        compilations.all {
            this.kotlinOptions.jvmTarget = kotlinOptions["jvmTarget"] as String
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.material3)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.core.ktx)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
            }
        }
        val desktopTest by getting
    }
}

android {
    val androidConfig: Map<String,Any> by extra
    buildToolsVersion = androidConfig["buildToolsVersion"] as String
        compileSdk = androidConfig["compileSdk"] as Int
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = androidConfig["minSdk"] as Int
        targetSdk = androidConfig["targetSdk"] as Int
    }
    compileOptions {
        val javaOptions: Map<String,Any> by extra
        compileOptions {
            sourceCompatibility = javaOptions["sourceCompatibility"] as JavaVersion
            targetCompatibility = javaOptions["targetCompatibility"] as JavaVersion
        }
    }
}