

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
}

kotlin {
    jvm {
        val main by compilations.getting {
            java.sourceCompatibility = JavaVersion.VERSION_1_8
            java.targetCompatibility = JavaVersion.VERSION_1_8
            kotlinOptions {
                // Setup the Kotlin compiler options for the 'main' compilation:
                jvmTarget = "1.8"
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
                api(project(":search:model"))
                api(project(":util"))
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
                implementation(Config.Libs.Retrofit.client)
                implementation(Config.Libs.Retrofit.converterKotlinxSerialization)
                api(Config.Libs.OkHttp.client)
                api(Config.Libs.okIo)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(Config.Libs.Test.truth)
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
