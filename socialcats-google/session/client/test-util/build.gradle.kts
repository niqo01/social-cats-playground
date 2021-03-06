

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    js {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                api(project(":session:client"))
                implementation(project(":store:client:test-util"))
                implementation(project(":auth:client:test-util"))

                api(Config.Libs.Kotlin.Coroutine.core)
                implementation(Config.Libs.KotlinLogging.common)
            }
        }

        val androidMain by getting {
            dependencies {

                api(Config.Libs.KotlinLogging.jdk)
            }
        }

        val jsMain by getting {
            dependencies {

                api(Config.Libs.KotlinLogging.js)
            }
        }
    }
}
