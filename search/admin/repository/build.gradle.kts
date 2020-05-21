import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = Config.CloudCommon.sourceCompatibility
    targetCompatibility = Config.CloudCommon.targetCompatibility
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Config.CloudCommon.kotlinJvmTarget
}

dependencies {
    api(Config.Libs.Kotlin.jdk8)
    api(Config.Libs.KotlinLogging.jdk)
    api(Config.Libs.Kotlin.Coroutine.jdk8)
    api(Config.Libs.elasticSearchHighLevelClient)
}
