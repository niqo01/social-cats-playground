plugins {
  kotlin("jvm")
}

group = "com.nicolasmilliard.repository.imageobjectstore"
version = "1.0-SNAPSHOT"

dependencies {
  implementation(project(":library:object-store"))
  api(kotlin("stdlib"))

  api("javax.inject:javax.inject:_")

  implementation("org.apache.logging.log4j:log4j-api:_")
  implementation("org.apache.logging.log4j:log4j-core:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")
}
