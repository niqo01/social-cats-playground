rootProject.name = "social-cats-playground"

include(":analytics")
include(":backend:ktor-api")
include(":backend:functions:auth-user-firestore")
include(":backend:functions:firestore-user-elasticsearch")
include(":store:common")
include(":store:admin")
include(":store:client")
include(":store:client:test-util")
include(":feature-flags")
include(":frontend:android")
include(":frontend:android:base")
include(":frontend:web")
include(":aws-request-signing")
include(":elasticsearch-request-signing")
include(":auth:client")
include(":auth:client:test-util")
include(":auth:ui")
include(":session:client")
include(":session:client:test-util")
include(":main-presenter")
include(":connectivity")
include(":connectivity:test-util")
include(":cloud-messaging:client")
include(":cloud-messaging:android")
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
include(":themes")
include(":kotlin-util")
include(":ui-android-util")
include(":test-settings")
include(":test-util")

pluginManagement {

    val kotlinVersion: String by settings
    val navigationVersion: String by settings

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            name = "Kotlin EAP (for kotlin-frontend-plugin)"
            url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
        }
        maven {
            url = uri("https://maven.fabric.io/public")
            content {
                includeModule("io.fabric.tools", "gradle")
            }
        }
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "androidx.navigation.safeargs.kotlin" ->
                    useModule("androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion")
                "kotlin-android", "kotlin-kapt" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
                "com.google.android.gms.oss-licenses-plugin" ->
                    useModule("com.google.android.gms:oss-licenses-plugin:0.10.1")
                "com.google.cloud.tools.appengine" -> useModule("com.google.cloud.tools:appengine-gradle-plugin:2.2.0")
                "kotlinx-serialization" -> useModule("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
            }
        }
    }
    plugins {
        id("org.jetbrains.kotlin.jvm") version "$kotlinVersion"
        id("org.jetbrains.kotlin.js") version "$kotlinVersion"
        id("org.jetbrains.kotlin.kapt") version "$kotlinVersion"
        id("org.jetbrains.kotlin.multiplatform") version "$kotlinVersion"
        id("org.jetbrains.kotlin.kotlinx-serialization") version "$kotlinVersion"
        id("com.github.johnrengelman.shadow") version "5.2.0"
        id("com.github.ben-manes.versions") version "0.28.0"
        id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    }
}
