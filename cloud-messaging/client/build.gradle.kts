

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android {
        publishAllLibraryVariants()
    }
//    js {
//        browser()
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
                implementation(project(":session:client"))
                api(Config.Libs.Kotlin.jdk8)
                api(Config.Libs.Kotlin.Coroutine.jdk8)
                api(Config.Libs.KotlinLogging.jdk)
                implementation(Config.Libs.Firebase.messaging)
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
