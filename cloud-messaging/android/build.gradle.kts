

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

dependencies {
    api(project(":frontend:android:base"))
    api(project(":cloud-messaging:client"))

    implementation(Config.Libs.Kotlin.jdk8)
    implementation(Config.Libs.Kotlin.Coroutine.android)
    implementation(Config.Libs.Firebase.messaging)
}
