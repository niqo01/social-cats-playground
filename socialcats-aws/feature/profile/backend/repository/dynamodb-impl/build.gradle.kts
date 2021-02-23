plugins {
  kotlin("jvm")
}

group = "com.nicolasmilliard.socialcatsaws.profile.backend.repository.dynamodb"
version = "1.0-SNAPSHOT"

dependencies {
  api(project(":feature:profile:backend:repository:dynamodb-impl:schema"))
  api(project(":feature:profile:backend:repository"))
  api(project(":library:cloud-metrics"))
  api(kotlin("stdlib"))

  api(platform("software.amazon.awssdk:bom:_"))
  api("software.amazon.awssdk:dynamodb-enhanced") {
    exclude(group = "software.amazon.awssdk", module = "http-client-spi")
    exclude(group = "software.amazon.awssdk", module = "apache-client")
    exclude(group = "software.amazon.awssdk", module = "netty-nio-client")
  }

  api("org.jetbrains.kotlinx:kotlinx-datetime:_")

  implementation("org.apache.logging.log4j:log4j-api:_")
  implementation("org.apache.logging.log4j:log4j-core:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")

  testImplementation(project(":library:cloud-metrics:fake"))
  testImplementation(platform("org.junit:junit-bom:_"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("com.amazonaws:DynamoDBLocal:_")
  testImplementation("com.almworks.sqlite4java:sqlite4java:_")
  testImplementation("software.amazon.awssdk:url-connection-client")
  testImplementation("org.apache.logging.log4j:log4j-slf4j18-impl:_")
}

configurations["testImplementation"].setCanBeResolved(true)
tasks.register<Copy>("copyNativeTestDeps") {
  from(configurations["testImplementation"]) {
    include("*.dll")
    include("*.dylib")
    include("*.so")
  }
  into("$buildDir/libs")
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed")
  }
  dependsOn(tasks["copyNativeTestDeps"])
}
