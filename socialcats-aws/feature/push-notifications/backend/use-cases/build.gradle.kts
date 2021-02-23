plugins {
  kotlin("jvm")
}

group = "com.nicolasmilliard.socialcatsaws.profile"
version = "1.0-SNAPSHOT"

dependencies {
  api(project(":feature:profile:backend:models"))
  api(project(":library:cloud-metrics"))
  implementation(project(":library:object-store"))
  implementation(project(":feature:profile:backend:repository"))
  api(kotlin("stdlib"))

  api("org.jetbrains.kotlinx:kotlinx-datetime:_")

  api("javax.inject:javax.inject:_")

  implementation("org.apache.logging.log4j:log4j-api:_")
  implementation("org.apache.logging.log4j:log4j-core:_")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")
}
