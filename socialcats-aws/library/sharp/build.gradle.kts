plugins {
  kotlin("jvm")
}

group = "com.nicolasmilliard.socialcatsaws.sharp"
version = "1.0-SNAPSHOT"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  sourceCompatibility = "11"
  targetCompatibility = "11"

  kotlinOptions {
    jvmTarget = "11"
    useIR = true
    freeCompilerArgs = listOf("-Xexplicit-api=strict")
  }
}

dependencies {
  api(kotlin("stdlib"))
  implementation("com.squareup.okio:okio:_")
}
