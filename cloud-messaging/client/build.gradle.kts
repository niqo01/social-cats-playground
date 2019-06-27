import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin {
    android()
//    js().compilations["main"].kotlinOptions {
//        moduleKind = "umd"
//    }
    sourceSets {
        commonMain {
            dependencies {
                api(project(":store:client"))
                api(project(":util"))
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
            }
        }

//        val jsMain by getting {
//            dependencies {
//                api(Config.Libs.Kotlin.js)
//                api(Config.Libs.Kotlin.Coroutine.js)
//                api(Config.Libs.KotlinLogging.js)
//            }
//        }
    }
}
