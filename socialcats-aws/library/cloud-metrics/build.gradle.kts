plugins {
  kotlin("jvm")
}

group = "com.nicolasmilliard.cloudmetric"
version = "1.0-SNAPSHOT"

dependencies {
  api(kotlin("stdlib"))

  implementation("software.amazon.cloudwatchlogs:aws-embedded-metrics:_")

  implementation("org.apache.logging.log4j:log4j-api:_")
  implementation("org.apache.logging.log4j:log4j-core:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")
}
