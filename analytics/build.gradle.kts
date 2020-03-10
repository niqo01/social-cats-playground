

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

base.archivesBaseName = "analytics"

kotlin {
    android()
    js {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                api(Config.Libs.Kotlin.common)
                implementation(Config.Libs.KotlinLogging.common)
            }
        }

        val androidMain by getting {
            dependencies {
                api(Config.Libs.Kotlin.jdk8)
                api(Config.Libs.KotlinLogging.jdk)
                api(Config.Libs.Firebase.analytics)
                api(Config.Libs.AndroidX.coreKtx)
            }
        }

        val jsMain by getting {
            dependencies {
                api(Config.Libs.Kotlin.js)
                api(Config.Libs.KotlinLogging.js)
            }
        }
    }
}
