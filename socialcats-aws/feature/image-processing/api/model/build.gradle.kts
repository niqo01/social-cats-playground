plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

kotlin {
  jvm {
    val main by compilations.getting {
      java.sourceCompatibility = JavaVersion.VERSION_11
      java.targetCompatibility = JavaVersion.VERSION_11
      kotlinOptions {
        jvmTarget = "11"
      }
    }
  }
  sourceSets {
    commonMain {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:_")
      }
    }
  }
}
