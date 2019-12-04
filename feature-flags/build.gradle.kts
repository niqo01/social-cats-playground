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
                implementation(project(":util"))
                api(Config.Libs.Kotlin.common)
                api(Config.Libs.Kotlin.Coroutine.common)
                implementation(Config.Libs.KotlinLogging.common)
            }
        }

        commonTest {
            dependencies {
                implementation(project(":test-util"))
                implementation(Config.Libs.Kotlin.Test.common)
                implementation(Config.Libs.Kotlin.Test.annotations)
            }
        }

        val androidMain by getting {
            dependencies {
                api(Config.Libs.Kotlin.jdk8)
                api(Config.Libs.Kotlin.Coroutine.jdk8)
                api(Config.Libs.KotlinLogging.jdk)
                implementation(Config.Libs.Firebase.remoteConfig)
                implementation(Config.Libs.timber)
                implementation(Config.Libs.Kotlin.Coroutine.playServices)
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(Config.Libs.Kotlin.Test.jdk)
                implementation(Config.Libs.Kotlin.Coroutine.test)
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
