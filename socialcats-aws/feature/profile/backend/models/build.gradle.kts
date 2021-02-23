plugins {
  kotlin("jvm")
}

group = "com.nicolasmilliard.socialcatsaws.backend.repository"
version = "1.0-SNAPSHOT"

dependencies {

  api(kotlin("stdlib"))

  api("org.jetbrains.kotlinx:kotlinx-datetime:_")

  implementation("org.apache.logging.log4j:log4j-api:_")
  implementation("org.apache.logging.log4j:log4j-core:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")
}
