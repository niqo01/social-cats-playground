
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.google.cloud.tools.appengine")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

val functionalTest: SourceSet by sourceSets.creating

val functionalTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val functionalTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

appengine {
    deploy {
        projectId = Config.GoogleCloud.projectId
        version = Config.Versions.SearchApi.version
    }
}

dependencies {
    implementation(project(":aws-request-signing"))
    evaluationDependsOn(":search:admin")
    implementation(project(":search:admin"))
    evaluationDependsOn(":search:admin:repository:test-util")
    implementation(project(":search:admin:repository:test-util"))
    implementation(Config.Libs.Kotlin.jdk8)
    implementation(Config.Libs.Ktor.ktorServerNetty)
    implementation(Config.Libs.Ktor.ktorSerialization)
    implementation(Config.Libs.Ktor.ktorAuth)
    implementation(Config.Libs.Ktor.ktorLocation)
    implementation(Config.Libs.Kotlin.Serialization.jdk)
    implementation(Config.Libs.logBackClassic)
    // Elastic search High level client uses log4j
    implementation(Config.Libs.log4jToSlf4j)
    implementation(Config.Libs.Firebase.admin)

    testImplementation(Config.Libs.Ktor.ktorServerTest)
    testImplementation(Config.Libs.Test.truth)
//    testImplementation(Config.Libs.OkHttp.client)

    functionalTestImplementation(Config.Libs.Test.junit)
    functionalTestImplementation(Config.Libs.Test.truth)
    functionalTestImplementation(Config.Libs.OkHttp.client)
}

tasks {
    jar {
        manifest {
            attributes("Main-Class" to "com.nicolasmilliard.socialcats.searchapi.ApplicationKt")
        }
        from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    val copyYaml = register("initConfig", Copy::class) {
        from("src/main/appengine") {
            include("env_variables.yaml")
        }
        into("build/staged-app")
    }

    appengineStage {
        finalizedBy(copyYaml)
    }

    register("functionalTest", Test::class) {
        description = "Run functional tests"
        group = "verification"

        testClassesDirs = functionalTest.output.classesDirs
        classpath = functionalTest.runtimeClasspath
        maxParallelForks = 1
    }
}
