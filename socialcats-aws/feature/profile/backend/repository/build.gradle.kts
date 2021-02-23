plugins {
  kotlin("jvm")
}

group = "com.nicolasmilliard.socialcatsaws.profile.backend.repository"
version = "1.0-SNAPSHOT"

dependencies {
  api(project(":feature:profile:backend:models"))
  api(kotlin("stdlib"))

  api("org.jetbrains.kotlinx:kotlinx-datetime:_")
}
