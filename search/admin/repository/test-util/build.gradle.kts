import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("kotlinx-serialization")
}

java {
    // (4)
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    api(project(":search:admin:repository"))
    api(Config.Libs.Kotlin.Serialization.jdk)
    api(Config.Libs.Kotlin.jdk8)
    api(Config.Libs.KotlinLogging.jdk)
}
