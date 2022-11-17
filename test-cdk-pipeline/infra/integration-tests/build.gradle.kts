plugins {
    id("com.nicolasmilliard.kotlin.application")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("junit.platform.output.capture.stdout", true)
    systemProperty("junit.platform.output.capture.stderr", true)
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.jdk8)
    implementation(libs.kotlin.coroutines.jvm)
    testImplementation(libs.retrofit)
    testImplementation(libs.retrofit.scalar.converter)
    testImplementation(libs.okhttp.curl)

    testImplementation(libs.kotlin.test)
}