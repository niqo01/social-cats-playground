plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
}

kotlin {
    jvm {
        val main by compilations.getting {
            java.sourceCompatibility = Config.Common.sourceCompatibility
            java.targetCompatibility = Config.Common.targetCompatibility
            kotlinOptions {
                jvmTarget = Config.Common.kotlinJvmTarget
            }
        }
    }
    js {
        browser()
        compilations["main"].kotlinOptions {
            freeCompilerArgs += listOf(
                "-Xuse-experimental=kotlinx.serialization.UnstableDefault"
            )
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                api(Config.Libs.Kotlin.common)
                api(Config.Libs.Kotlin.Coroutine.common)
                implementation(Config.Libs.Kotlin.Serialization.common)
            }
        }
        val jvmMain by getting {
            dependencies {
                api(Config.Libs.Kotlin.jdk8)
                api(Config.Libs.Kotlin.Coroutine.jdk8)
                implementation(Config.Libs.Kotlin.Serialization.jdk)
                api(Config.Libs.Retrofit.client)
                api(Config.Libs.OkHttp.client)
                implementation(Config.Libs.Retrofit.converterKotlinxSerialization)
            }
        }

        val jsMain by getting {
            dependencies {
                api(Config.Libs.Kotlin.js)
                api(Config.Libs.Kotlin.Coroutine.js)
                api(Config.Libs.Kotlin.Serialization.js)
            }
        }
    }
}
