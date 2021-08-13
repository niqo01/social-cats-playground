plugins {
    kotlin("jvm")
    application
}

val kotlinJvmTarget: String
    get() = extra["kotlin.jvm.target"].toString()

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = kotlinJvmTarget

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.jdk8)

    implementation(libs.aws.cdk)
}

application {
    mainClass.set("com.nicolasmilliard.testcdkpipeline.AppKt")
}