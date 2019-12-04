import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

java {
    sourceCompatibility = Config.GoogleCloud.Functions.sourceCompatibility
    targetCompatibility = Config.GoogleCloud.Functions.targetCompatibility
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Config.GoogleCloud.Functions.kotlinJvmTarget
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
}

dependencies {
    implementation(project(":aws-request-signing"))
    implementation(project(":elasticsearch-request-signing"))
    implementation(project(":search:admin"))
    implementation(project(":store:common"))

    implementation(Config.Libs.Kotlin.jdk8)
    implementation(Config.Libs.Firebase.admin)

    implementation(Config.Libs.KotlinLogging.js)
    implementation(Config.Libs.logBackClassic)
    // Elastic search High level client uses log4j
    implementation(Config.Libs.log4jToSlf4j)

    // Cloud function deps
    implementation(Config.Libs.GoogleFunction.gson)

    compileOnly(Config.Libs.GoogleFunction.javaServletApi)
    compileOnly(Config.Libs.GoogleFunction.functionFrameworkApi)

    testImplementation(Config.Libs.GoogleFunction.javaServletApi)
    testImplementation(Config.Libs.GoogleFunction.functionFrameworkApi)
    testImplementation(Config.Libs.Test.junit)
    testImplementation(Config.Libs.Test.truth)
}
