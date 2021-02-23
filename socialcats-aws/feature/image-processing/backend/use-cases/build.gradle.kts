plugins {
  kotlin("jvm")
}

group = "com.nicolasmilliard.socialcatsaws.profile.image"
version = "1.0-SNAPSHOT"

dependencies {
  api(project(":feature:profile:backend:use-cases"))
  api(project(":library:cloud-metrics"))
  implementation(project(":library:image-object-store"))
  implementation(project(":feature:profile:backend:repository"))

  api(kotlin("stdlib"))

  implementation("org.apache.logging.log4j:log4j-api:_")
  implementation("org.apache.logging.log4j:log4j-core:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")
}
