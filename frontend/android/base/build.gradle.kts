import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {

    api(project(":auth:client"))
    api(project(":auth:ui"))
    api(project(":store:client"))
    api(project(":session:client"))
    api(project(":connectivity"))
    api(project(":cloud-messaging:client"))

    api(Config.Libs.Kotlin.jdk8)
    api(Config.Libs.Kotlin.Coroutine.android)

    api(Config.Libs.playCore)

    api(Config.Libs.AndroidX.coreKtx)
    api(Config.Libs.AndroidX.constraintLayout)
    api(Config.Libs.AndroidX.vectorCompat)

    api(Config.Libs.Firebase.common)
    api(Config.Libs.Firebase.analytics)
    api(Config.Libs.Firebase.crashlytics)
    api(Config.Libs.Firebase.performance)
    api(Config.Libs.Firebase.inAppMessaging)

    api(Config.Libs.OkHttp.client)
    api(Config.Libs.OkHttp.logging)

    api(Config.Libs.byteUnits)
    api(Config.Libs.slf4jTimber)
    api(Config.Libs.timber)

    api(Config.Libs.material)
}
