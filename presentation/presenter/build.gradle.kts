plugins {
    kotlin("multiplatform")
}

base.archivesBaseName = "presentation-presenter"

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
            }
        }
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                api(Config.Libs.Kotlin.jdk8)
                api(Config.Libs.Kotlin.Coroutine.jdk8)
            }
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(Config.Libs.Test.truth)
            }
        }
        js().compilations["main"].defaultSourceSet {
            dependencies {
                api(Config.Libs.Kotlin.js)
                api(Config.Libs.Kotlin.Coroutine.js)
            }
        }
    }
}
