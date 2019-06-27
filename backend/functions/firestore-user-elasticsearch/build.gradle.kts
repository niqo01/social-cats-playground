import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

// tasks.getByName("build").dependsOn( tasks.getByName("shadowJar"))

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
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
