

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

base.archivesBaseName = "search-presenter"

kotlin {
    android()
    js {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":session:client"))
                implementation(project(":connectivity"))
                implementation(project(":search:client"))
                api(project(":search:model"))
                api(project(":presentation:presenter"))
                implementation(project(":util"))
                api(Config.Libs.Kotlin.common)
                api(Config.Libs.Kotlin.Coroutine.common)
                implementation(Config.Libs.KotlinLogging.common)
            }
        }

        commonTest {
            dependencies {
                implementation(project(":test-util"))
                implementation(project(":store:client:test-util"))
                implementation(project(":auth:client:test-util"))
                implementation(project(":session:client:test-util"))
                implementation(project(":connectivity:test-util"))
                implementation(project(":api:social-cats:test-util"))
                implementation(Config.Libs.Kotlin.Test.common)
                implementation(Config.Libs.Kotlin.Test.annotations)
            }
        }

        val androidMain by getting {
            dependencies {
                api(Config.Libs.Kotlin.jdk8)
                api(Config.Libs.Kotlin.Coroutine.jdk8)
                api(Config.Libs.KotlinLogging.jdk)
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
