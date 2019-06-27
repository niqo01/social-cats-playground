import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    api(Config.Libs.Kotlin.jdk8)
    api(Config.Libs.Kotlin.Coroutine.jdk8)
    api(Config.Libs.KotlinLogging.jdk)
    api(Config.Libs.Firebase.admin)
}
