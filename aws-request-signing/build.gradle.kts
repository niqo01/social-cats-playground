import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    api(Config.Libs.Kotlin.jdk8)
    api(Config.Libs.KotlinLogging.jdk)
    api(platform(Config.Libs.Aws.sdkBom))
    api(Config.Libs.Aws.apacheClient)
    api(Config.Libs.Aws.sdkAuth)
}
