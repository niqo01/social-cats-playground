plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
//    js {
//        browser()
//    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":api:social-cats"))
                api(project(":payment:model"))
                api(Config.Libs.Kotlin.common)
                api(Config.Libs.Kotlin.Coroutine.common)
                implementation(Config.Libs.KotlinLogging.common)
                api(project(":kotlin-util"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(Config.Libs.Retrofit.client)
                implementation(Config.Libs.Retrofit.converterKotlinxSerialization)
                api(Config.Libs.OkHttp.client)
                api(Config.Libs.Kotlin.jdk8)
                api(Config.Libs.Kotlin.Coroutine.jdk8)
                api(Config.Libs.KotlinLogging.jdk)
                api(Config.Libs.Stripe.android)
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(Config.Libs.Kotlin.Test.jdk)
                implementation(Config.Libs.Kotlin.Coroutine.test)
            }
        }
//        js().compilations["main"].defaultSourceSet {
//            dependencies {
//                api(Config.Libs.Kotlin.js)
//                api(Config.Libs.Kotlin.Coroutine.js)
//                api(Config.Libs.KotlinLogging.js)
//            }
//        }
    }
}
