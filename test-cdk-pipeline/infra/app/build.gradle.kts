plugins {
    id("com.nicolasmilliard.kotlin.application")
    application
}

val lambdaConf: Configuration by configurations.creating {
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.jdk8)

    implementation(libs.aws.cdk)
    implementation(libs.aws.cdk.appsync.alpha)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.engine)
    testImplementation(libs.assertj.core)

    lambdaConf(
        group = "com.nicolasmilliard.testcdkpipeline",
        name = "get-data-lambda",
        version = "0.0.21",
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
        val file = File("$buildDir/lambdas.properties")
        file.parentFile.mkdirs()
        file.createNewFile()
        file.printWriter().use { out ->
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