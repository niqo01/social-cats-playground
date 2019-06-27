plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
}

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
                implementation(Config.Libs.Kotlin.Serialization.common)
            }
        }
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                api(Config.Libs.Kotlin.jdk8)
                implementation(Config.Libs.Kotlin.Serialization.jdk)
            }
        }
        js().compilations["main"].defaultSourceSet {
            dependencies {
                api(Config.Libs.Kotlin.js)
                api(Config.Libs.Kotlin.Serialization.js)
            }
        }
    }
}
