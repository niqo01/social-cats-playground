plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android {
        publishAllLibraryVariants()
    }
    js {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                api(project(":feature-flags"))
                api(Config.Libs.Kotlin.common)
                api(Config.Libs.Kotlin.Coroutine.common)
                implementation(Config.Libs.KotlinLogging.common)
            }
        }

        val androidMain by getting {
            dependencies {
                api(Config.Libs.Kotlin.jdk8)
                api(Config.Libs.Kotlin.Coroutine.jdk8)
                api(Config.Libs.KotlinLogging.jdk)
                implementation(Config.Libs.Firebase.remoteConfig)
                implementation(Config.Libs.material)
                implementation(Config.Libs.AndroidX.constraintLayout)
                implementation(Config.Libs.processPhoenix)
            }
        }

        val jsMain by getting {
            dependencies {
                api(Config.Libs.Kotlin.js)
                api(Config.Libs.Kotlin.Coroutine.js)
                api(Config.Libs.KotlinLogging.js)
            }
        }
    }
}
