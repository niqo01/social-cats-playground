plugins {
  kotlin("jvm")
  kotlin("kapt")
  id("com.squareup.anvil")
}

group = "com.nicolasmilliard.repository.objectstore.fake.wiring"
version = "1.0-SNAPSHOT"

dependencies {
  api(project(":library:object-store:fake"))
  api(kotlin("stdlib"))

  implementation("com.google.dagger:dagger:_")
  kapt("com.google.dagger:dagger-compiler:_")

  implementation("org.apache.logging.log4j:log4j-api:_")
  implementation("org.apache.logging.log4j:log4j-core:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")
}
