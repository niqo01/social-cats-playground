import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {

    api(project(":analytics"))
    implementation(project(":auth:client"))
    implementation(project(":auth:ui"))
    implementation(project(":store:client"))
    implementation(project(":session:client"))
    implementation(project(":bug-reporter"))
    implementation(project(":connectivity"))
    implementation(project(":cloud-messaging:client"))
    implementation(project(":cloud-messaging:android"))
    implementation(project(":presentation:binder"))
    implementation(project(":main-presenter"))
    implementation(project(":ui-android-util"))
    implementation(project(":feature-flags"))
    implementation(project(":themes"))

    api(Config.Libs.Kotlin.jdk8)
    api(Config.Libs.Kotlin.Coroutine.android)

    implementation(Config.Libs.playCore)

    implementation(Config.Libs.AndroidX.appCompat)
    implementation(Config.Libs.AndroidX.activityKtx)
    implementation(Config.Libs.AndroidX.fragmentKtx)
    implementation(Config.Libs.AndroidX.preferenceKtx)
    implementation(Config.Libs.AndroidX.coreKtx)
    implementation(Config.Libs.AndroidX.constraintLayout)
    implementation(Config.Libs.AndroidX.vectorCompat)
    api(Config.Libs.AndroidX.navigationFragmentKtx)
    api(Config.Libs.AndroidX.navigationUiKtx)
    implementation(Config.Libs.AndroidX.viewModelKtx)
    implementation(Config.Libs.AndroidX.lifecycleKtx)
    implementation(Config.Libs.AndroidX.lifecycleCommon)

    implementation(Config.Libs.Firebase.common)
    implementation(Config.Libs.Firebase.performance)
    implementation(Config.Libs.Firebase.inAppMessaging)

    implementation(Config.Libs.OkHttp.client)
    implementation(Config.Libs.OkHttp.logging)

    implementation(Config.Libs.byteUnits)
    implementation(Config.Libs.slf4jTimber)
    implementation(Config.Libs.timber)

    implementation(Config.Libs.coil)
}
