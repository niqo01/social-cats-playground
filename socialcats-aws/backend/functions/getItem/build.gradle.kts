import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

group = "com.nicolasmilliard.socialcatsaws.backend.functions"
version = "1.0-SNAPSHOT"

tasks.withType<Jar> {
    manifest {
        attributes["Multi-Release"] = "true"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(platform("software.amazon.awssdk:bom:2.10.73"))
    implementation("software.amazon.awssdk:lambda")
    implementation("com.amazonaws:aws-lambda-java-core:_")
    implementation("com.amazonaws:aws-lambda-java-events:_")

    implementation("org.apache.logging.log4j:log4j-api:_")
    implementation("org.apache.logging.log4j:log4j-core:_")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:_")
    runtimeOnly("com.amazonaws:aws-lambda-java-log4j2:_")
    testImplementation("org.junit.jupiter:junit-jupiter-api:_")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:_")
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer())
}


//
artifacts {
    add("shadow", tasks["shadowJar"]) {
        builtBy(tasks["shadowJar"])
    }
}