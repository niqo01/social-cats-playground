plugins {
    kotlin("jvm")
    application
}

val kotlinJvmTarget: String
    get() = extra["kotlin.jvm.target"].toString()

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = kotlinJvmTarget
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.jdk8)

}


application {
    mainClass.set("com.nicolasmilliard.testcdkpipeline.AppKt")
}