

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

base.archivesBaseName = "di"

kotlin {
    android()
    js {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                api(project(":analytics"))
                implementation(project(":auth:client"))
                implementation(project(":store:client"))
                implementation(project(":session:client"))
                implementation(project(":bug-reporter"))
                implementation(project(":connectivity"))
                implementation(project(":cloud-messaging:client"))
                implementation(project(":feature-flags"))
                api(Config.Libs.Kotlin.common)
                api(Config.Libs.Kotlin.Coroutine.common)
                implementation(Config.Libs.KotlinLogging.common)
                api(Config.Libs.Koin.core)
            }
        }

        commonTest {
            dependencies {
                implementation(project(":test-util"))
                implementation(Config.Libs.Kotlin.Test.common)
                implementation(Config.Libs.Kotlin.Test.annotations)
                implementation(Config.Libs.Koin.test)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(project(":cloud-messaging:android"))

                api(Config.Libs.Kotlin.jdk8)
                api(Config.Libs.Kotlin.Coroutine.jdk8)
                api(Config.Libs.KotlinLogging.jdk)

                implementation(Config.Libs.OkHttp.client)
                implementation(Config.Libs.OkHttp.logging)
                implementation(Config.Libs.byteUnits)
                implementation(Config.Libs.coil)
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

        val jsTest by getting {
            dependencies {
                implementation(Config.Libs.Kotlin.Test.js)
            }
        }
    }
}
