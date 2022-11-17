plugins {
    id("com.nicolasmilliard.kotlin.application")
    application
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.jdk8)

    implementation(libs.aws.cdk)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.nicolasmilliard.testcdkpipeline.AppKt")
}