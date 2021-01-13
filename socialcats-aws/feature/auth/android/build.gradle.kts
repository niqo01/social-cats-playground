

plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.kapt")
  id("dagger.hilt.android.plugin")
}

android {
  compileSdkVersion(30)

  defaultConfig {
    minSdkVersion(findProperty("minSdkVersion") as String)
    targetSdkVersion(findProperty("targetSdkVersion") as String)
  }
  compileOptions {
    // Using java 11 fails the build with Concurrent modification exception
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
    freeCompilerArgs = listOf("-Xexplicit-api=strict")
    useIR = true
  }
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:_")
  implementation("com.jakewharton.timber:timber:_")
  implementation("com.amplifyframework:aws-auth-cognito:_")
  api("com.google.dagger:hilt-android:_")
  kapt("com.google.dagger:hilt-android-compiler:_")
  implementation("androidx.startup:startup-runtime:_")
}
