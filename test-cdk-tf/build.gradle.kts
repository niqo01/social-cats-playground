plugins {
    alias(libs.plugins.bencheck)
    alias(libs.plugins.kotlin.jvm)
    application
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "17"

dependencies {
    implementation(libs.cdktf)
    implementation(libs.constructs)
    implementation(libs.cdktf.provider.google)

    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
}

application {
    mainClass.set("com.nicolasmilliard.testcdktfpipeline.AppKt")
}