import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.nicolasmilliard.publish.checkIfAlreadyPublished

plugins {
    id("com.nicolasmilliard.kotlin.application")
    id("com.nicolasmilliard.publish")
    alias(libs.plugins.kotlinserialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.aspectj)
    alias(libs.plugins.graalvm)
    distribution
}

group = "com.nicolasmilliard.testcdkpipeline"
version = "0.0.1"
val artifactName = "presence-status-lambda"

kotlin {
//    jvmToolchain{
//        languageVersion.set(JavaLanguageVersion.of(11))
//        vendor.set(JvmVendorSpec.GRAAL_VM)
//    }
}

tasks.test {
    useJUnitPlatform()
    environment(
        mapOf(
            "AWS_XRAY_CONTEXT_MISSING" to "LOG_ERROR",
            "POWERTOOLS_TRACER_CAPTURE_RESPONSE" to "false",
            "POWERTOOLS_TRACER_CAPTURE_ERROR" to "false",
        )
    )
    systemProperties(
        mapOf(
            "software.amazon.awssdk.http.service.impl" to "software.amazon.awssdk.http.urlconnection.UrlConnectionSdkHttpService",
        )
    )
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer())
    exclude("META-INF/maven/**")
    exclude("META-INF/proguard/**")
    exclude("META-INF/**/module-info.class")
}

graalvmNative {
    binaries {
        named("main") {
//            javaLauncher.set(javaToolchains.launcherFor {
//                languageVersion.set(JavaLanguageVersion.of(11))
//                vendor.set(JvmVendorSpec.GRAAL_VM)
//            })
            requiredVersion.set("22.3.0")
            mainClass.set("com.amazonaws.services.lambda.runtime.api.client.AWSLambda")
            buildArgs.apply {
                add("--verbose")
                add("--no-fallback")
                add("--initialize-at-build-time=org.slf4j")
                add("--enable-url-protocols=http")
                add("-H:+AllowIncompleteClasspath")
                add("-H:+ReportExceptionStackTraces")
            }
        }
    }
}

distributions {
    main {
        distributionBaseName.set("distribution")
        contents {
            from(layout.buildDirectory.dir("native/nativeCompile"))
            from(layout.projectDirectory.dir("src/main/config"))
        }
    }
}

tasks["distZip"].dependsOn("nativeCompile")


dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.jdk8)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.okio)

    implementation(libs.aws.sdk.kotlin.dynamodb.client)
    implementation(libs.aws.lambda.javaRuntimeInterfaceClient)
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
            artifact(tasks.distZip)
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf {
        return@onlyIf checkIfAlreadyPublished(artifactName)
    }
}

tasks["publish"].dependsOn(tasks["check"])
