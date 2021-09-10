plugins {
    kotlin("jvm")
}

val kotlinJvmTarget: String
    get() = extra["kotlin.jvm.target"].toString()

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = kotlinJvmTarget
}

tasks.test {
    useJUnitPlatform()
    systemProperty("junit.platform.output.capture.stdout", true)
    systemProperty("junit.platform.output.capture.stderr", true)
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.jdk8)
    implementation(libs.kotlin.coroutines)
    testImplementation(libs.retrofit)
    testImplementation(libs.retrofit.scalar.converter)
    testImplementation(libs.okhttp.curl)

    testImplementation(libs.kotlin.test)
}