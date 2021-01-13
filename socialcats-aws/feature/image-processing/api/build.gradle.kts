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
  api(project(":feature:image-processing:api:model"))
  api(kotlin("stdlib"))
  api("com.squareup.retrofit2:retrofit:_")
}
