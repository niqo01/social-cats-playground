plugins {
  kotlin("jvm")
}

group = "com.nicolasmilliard.repository.objectstore.s3"
version = "1.0-SNAPSHOT"

dependencies {
  api(project(":library:object-store"))
  api(kotlin("stdlib"))

  api(platform("software.amazon.awssdk:bom:_"))
  api("software.amazon.awssdk:s3") {
    exclude(group = "software.amazon.awssdk", module = "http-client-spi")
    exclude(group = "software.amazon.awssdk", module = "apache-client")
    exclude(group = "software.amazon.awssdk", module = "netty-nio-client")
  }

  implementation("org.apache.logging.log4j:log4j-api:_")
  implementation("org.apache.logging.log4j:log4j-core:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")
}
