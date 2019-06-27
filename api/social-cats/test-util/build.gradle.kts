plugins {
    kotlin("multiplatform")
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
    }
    sourceSets {
        commonMain {
            dependencies {
                api(Config.Libs.Kotlin.common)
                api(Config.Libs.Kotlin.Coroutine.common)
                api(project(":api:social-cats"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api(Config.Libs.Kotlin.jdk8)
                api(Config.Libs.Kotlin.Coroutine.jdk8)
            }
        }

        val jsMain by getting {
            dependencies {
                api(Config.Libs.Kotlin.js)
                api(Config.Libs.Kotlin.Coroutine.js)
            }
        }
    }
}
