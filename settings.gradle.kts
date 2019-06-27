rootProject.name = "social-cats-playground"

include(":backend:ktor-api")
include(":backend:functions:auth-user-firestore")
include(":backend:functions:firestore-user-elasticsearch")
include(":store:admin")
include(":store:client")
include(":store:client:test-util")
include(":frontend:android")
include(":frontend:android:base")
include(":frontend:web")
include(":aws-request-signing")
include(":auth:client")
include(":auth:client:test-util")
include(":auth:ui")
include(":session")
include(":connectivity")
include(":connectivity:test-util")
include(":account:presenter")
include(":account:ui-android")
include(":search:model")
include(":search:admin")
include(":search:admin:repository")
include(":search:admin:repository:test-util")
include(":search:client")
include(":search:presenter")
include(":search:ui-android")
include(":api:social-cats")
include(":api:social-cats:test-util")
include(":presentation:presenter")
include(":presentation:binder")
include(":test-util")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            name = "Kotlin EAP (for kotlin-frontend-plugin)"
            url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
        }
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.jetbrains.kotlin.frontend" -> useModule(Config.Plugins.kotlinFrontEnd)
                "kotlin-dce-js", "kotlin2js", "kotlin-android", "kotlin-kapt" -> useModule(Config.Plugins.kotlinGradle)
                "com.google.android.gms.oss-licenses-plugin" -> useModule(Config.Plugins.ossLicenses)
                "com.google.cloud.tools.appengine" -> useModule(Config.Plugins.appEngineGradlePlugin)
                "com.android.application" -> useModule(Config.Plugins.android)
                "androidx.navigation.safeargs.kotlin" -> useModule(Config.Plugins.navigation)
                "kotlinx-serialization" -> useModule(Config.Plugins.kotlinSerialization)
            }
        }
    }
    plugins {
        id("org.jetbrains.kotlin.jvm") version Config.kotlinVersion
        id("org.jetbrains.kotlin.kapt") version Config.kotlinVersion
        id("org.jetbrains.kotlin.multiplatform") version Config.kotlinVersion
        id("org.jetbrains.kotlin.kotlinx-serialization") version Config.kotlinVersion
        id("com.github.johnrengelman.shadow") version Config.Plugins.shadowVersion
        id("com.github.ben-manes.versions") version Config.Plugins.gradleVersions
        id("org.jlleitschuh.gradle.ktlint") version Config.Plugins.ktlintPluginVersion
    }
}
