plugins {
  kotlin("jvm")
}

group = "com.nicolasmilliard.repository.objectstore.fake"
version = "1.0-SNAPSHOT"

dependencies {
  api(project(":library:object-store"))
  api(kotlin("stdlib"))

  implementation("org.apache.logging.log4j:log4j-api:_")
  implementation("org.apache.logging.log4j:log4j-core:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")
}
