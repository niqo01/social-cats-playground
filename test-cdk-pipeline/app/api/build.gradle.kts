import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinserialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.aspectj)
    id("maven-publish")
}

group = "com.nicolasmilliard.testcdkpipeline"
version = "0.0.21"
val artifactName = "get-data-lambda"

val kotlinJvmTarget: String
    get() = extra["kotlin.jvm.target"].toString()

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    configure<io.freefair.gradle.plugins.aspectj.AjcAction> {
        options.compilerArgs.add("-source")
        options.compilerArgs.add("11")
    }
}

tasks.test {
    useJUnitPlatform()
    environment(mapOf(
        "AWS_XRAY_CONTEXT_MISSING" to "LOG_ERROR",
        "POWERTOOLS_TRACER_CAPTURE_RESPONSE" to "false",
        "POWERTOOLS_TRACER_CAPTURE_ERROR" to "false",
    ))
    systemProperties(mapOf(
        "software.amazon.awssdk.http.service.impl" to "software.amazon.awssdk.http.urlconnection.UrlConnectionSdkHttpService",
    ))
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer())
    exclude("META-INF/maven/**")
    exclude("META-INF/proguard/**")
    exclude("META-INF/**/module-info.class")
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.jdk8)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.okio)

    implementation(libs.aws.sdk.kotlin.dynamodb.client)
//    {
//        exclude(group = "software.amazon.awssdk", module = "apache-client")
//        exclude(group = "software.amazon.awssdk", module = "netty-nio-client")
//    }


    implementation(libs.aws.lambda.java.core)
    implementation(libs.aws.lambda.java.events)
//    implementation(libs.aws.lambda.powertools.tracing) {
//        exclude(group = "com.amazonaws", module = "aws-xray-recorder-sdk-aws-sdk-v2-instrumentor")
//    }
//    aspect(libs.aws.lambda.powertools.tracing) {
//        exclude(group = "com.amazonaws", module = "aws-xray-recorder-sdk-aws-sdk-v2-instrumentor")
//    }
    implementation(libs.aws.lambda.powertools.logging)
    aspect(libs.aws.lambda.powertools.logging)
    implementation(libs.aws.lambda.powertools.metrics)
    aspect(libs.aws.lambda.powertools.metrics)

    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.kotlin.logging.jvm)

    runtimeOnly(libs.aws.lambda.java.log4j2)
    runtimeOnly(libs.log4j.slf4j18.impl)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.aws.sdk.url.connection.client)
    testImplementation(libs.tempest2)
    testImplementation(libs.tempest2.testing.jvm)
    testImplementation(libs.tempest2.testing.junit5)
    testImplementation(libs.aws.lambda.java.tests)
    testImplementation(libs.assertj.core)
//    testImplementation(libs.aws.sdk.dynamodb.enhanced)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = artifactName

            setArtifacts(listOf(tasks.named("shadowJar").get()))
        }
    }
    repositories {
        maven {
            url =
                uri("https://test-domain-480917579245.d.codeartifact.us-east-1.amazonaws.com/maven/test-repository/")
            credentials {
                username = "aws"
                password = System.getenv("CODEARTIFACT_AUTH_TOKEN")
            }
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf {
        val eout = ByteArrayOutputStream()
        val res = exec {
            commandLine(
                "aws",
                "codeartifact",
                "describe-package-version",
                "--domain", "test-domain",
                "--repository", "test-repository",
                "--format", "maven",
                "--namespace", "${project.group}",
                "--package", "$artifactName",
                "--package-version", "${project.version}"
            )
            isIgnoreExitValue = true
            standardOutput = ByteArrayOutputStream()
            errorOutput = eout
        }
        val errorOutput = eout.toString(StandardCharsets.UTF_8)
        when {
            res.exitValue == 254 && errorOutput.contains("ResourceNotFoundException") -> true
            res.exitValue == 0 -> false
            else -> {
                res.rethrowFailure()
                false
            }
        }
    }
}

tasks["publish"].dependsOn(tasks["check"])
