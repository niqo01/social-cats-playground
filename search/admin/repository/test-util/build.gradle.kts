import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("kotlinx-serialization")
}

java {
    sourceCompatibility = Config.CloudCommon.sourceCompatibility
    targetCompatibility = Config.CloudCommon.targetCompatibility
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Config.CloudCommon.kotlinJvmTarget
}

dependencies {
    api(project(":search:admin:repository"))
    api(Config.Libs.Kotlin.Serialization.jdk)
    api(Config.Libs.Kotlin.jdk8)
    api(Config.Libs.KotlinLogging.jdk)
}
