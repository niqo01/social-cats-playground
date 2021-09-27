import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  kotlin("jvm")
  kotlin("kapt")
  kotlin("plugin.serialization")
  id("com.github.johnrengelman.shadow")
  id("com.squareup.anvil")
}

version = "1.0-SNAPSHOT"

tasks.withType<ShadowJar> {
  mergeServiceFiles()
  transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer())
  exclude("META-INF/maven/**")
  exclude("META-INF/proguard/**")
  exclude("META-INF/**/module-info.class")
}

tasks.register<proguard.gradle.ProGuardTask>("proguard") {
  configuration("proguard.cfg")
  injars("$buildDir/libs/${project.name}-$version-all.jar")
  outjars("$buildDir/libs/${project.name}-$version-proguard.jar")
  libraryjars("${System.getProperty("java.home")}/jmods")
}

tasks["proguard"].dependsOn(tasks["shadowJar"])

val proguard by configurations.creating {
}

//
artifacts {
  add("proguard", file("$buildDir/libs/${project.name}-$version-proguard.jar")) {
    builtBy(tasks["proguard"])
  }
  add("shadow", tasks["shadowJar"]) {
    builtBy(tasks["shadowJar"])
  }
}

dependencies {
  implementation(project(":library:di-scope"))
  implementation(project(":library:image-object-store"))
  implementation(project(":feature:image-processing:api:model"))
  implementation(project(":feature:image-processing:backend:use-cases"))

  implementation(project(":feature:profile:backend:repository:dynamodb-wiring"))
  implementation(project(":library:object-store:s3-wiring"))

  implementation(kotlin("stdlib"))
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:_")

  implementation("com.google.dagger:dagger:_")
  kapt("com.google.dagger:dagger-compiler:_")

  implementation(platform("software.amazon.awssdk:bom:_"))
  implementation("software.amazon.awssdk:lambda")

  implementation("com.amazonaws:aws-lambda-java-core:_")
  implementation("com.amazonaws:aws-lambda-java-events:_")

  implementation("software.amazon.awssdk:url-connection-client")

  implementation(platform("com.amazonaws:aws-xray-recorder-sdk-bom:_"))
  implementation("com.amazonaws:aws-xray-recorder-sdk-core")
  implementation("com.amazonaws:aws-xray-recorder-sdk-aws-sdk-core")
  implementation("com.amazonaws:aws-xray-recorder-sdk-aws-sdk-v2")

  implementation("org.apache.logging.log4j:log4j-api:_")
  implementation("org.apache.logging.log4j:log4j-core:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")

  runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:_")
  runtimeOnly("com.amazonaws:aws-lambda-java-log4j2:_")

  testImplementation("org.junit.jupiter:junit-jupiter-api:_")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:_")
}
