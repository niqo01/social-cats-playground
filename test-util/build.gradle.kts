plugins {
    kotlin("multiplatform")
}

base.archivesBaseName = "common-test"

kotlin {
    jvm {
        val main by compilations.getting {
            java.sourceCompatibility = Config.Android.sourceCompatibility
            java.targetCompatibility = Config.Android.targetCompatibility
            kotlinOptions {
                jvmTarget = Config.Android.kotlinJvmTarget
            }
        }
    }
    js {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                api(Config.Libs.Kotlin.common)
                api(Config.Libs.Kotlin.Coroutine.common)
                implementation(Config.Libs.KotlinLogging.common)
                api(Config.Libs.turbine)
            }
        }

        val jvmMain by getting {
            dependencies {
                api(Config.Libs.Kotlin.jdk8)
                api(Config.Libs.Kotlin.Coroutine.jdk8)
                api(Config.Libs.KotlinLogging.jdk)
                api(Config.Libs.slf4jSimple)
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
