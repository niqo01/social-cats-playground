repositories {
    maven {
        url = uri("https://test-domain-480917579245.d.codeartifact.us-east-1.amazonaws.com/maven/test-repository/")
        credentials {
            username = "aws"
            password = System.getenv("CODEARTIFACT_AUTH_TOKEN")
        }
    }
}

plugins {
    kotlin("jvm")
    application
}

val kotlinJvmTarget: String
    get() = extra["kotlin.jvm.target"].toString()

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = kotlinJvmTarget
}

val lambdaConf by configurations.creating {
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.jdk8)

    implementation(libs.aws.cdk)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.engine)
    testImplementation(libs.assertj.core)

    lambdaConf(
        group = "com.nicolasmilliard.testcdkpipeline",
        name = "get-data-lambda",
        version = "0.0.18",
        classifier = "all"
    )

    lambdaConf(
        group = "com.nicolasmilliard.testcdkpipeline",
        name = "lambda-tiered-compilation-layer",
        version = "0.0.1"
    )
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("generateLambdaProperties") {
    doLast {
        val f = File("$buildDir/lambdas.properties")
            .printWriter().use { out ->
                lambdaConf.dependencies.forEach {
                    val files = lambdaConf.files(it)
                    out.println("${it.group}_${it.name}=${files.iterator().next().absolutePath}")
                }
            }
    }
}

tasks["run"].dependsOn(tasks["generateLambdaProperties"])

application {
    mainClass.set("com.nicolasmilliard.testcdkpipeline.AppKt")
}

tasks.withType<JavaExec>().configureEach {
    args = listOf("$buildDir/lambdas.properties")
}