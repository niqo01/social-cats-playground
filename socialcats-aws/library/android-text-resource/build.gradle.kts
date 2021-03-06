plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
}

android {
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = Config.Android.composeVersion
  }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:_")
  implementation(AndroidX.compose.ui)
}
