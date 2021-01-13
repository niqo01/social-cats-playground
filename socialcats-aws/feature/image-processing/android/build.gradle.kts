plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.kapt")
  id("dagger.hilt.android.plugin")
  kotlin("plugin.serialization")
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
    freeCompilerArgs += "-Xexplicit-api=strict"
    freeCompilerArgs += "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
    freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"

    useIR = true
  }
}

dependencies {
  implementation(project(":feature:auth:android"))
  implementation(project(":feature:image-processing:api"))
  implementation(project(":feature:image-processing:api:model"))
  api(project(":library:sharp"))
  api("org.jetbrains.kotlinx:kotlinx-coroutines-android:_")
  implementation("com.jakewharton.timber:timber:_")
  implementation("com.github.dhaval2404:imagepicker:_")
  api("androidx.work:work-runtime-ktx:_")
  implementation("com.squareup.retrofit2:retrofit:_")
  implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:_")
  api("com.squareup.okhttp3:okhttp:_")
  api("com.google.dagger:hilt-android:_")
  api("androidx.hilt:hilt-work:_")
  kapt("androidx.hilt:hilt-compiler:_")
  kapt("com.google.dagger:hilt-compiler:_")
  implementation("io.coil-kt:coil:_")
}
