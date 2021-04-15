plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.kapt")
  id("dagger.hilt.android.plugin")
}

android {
  composeOptions {
    kotlinCompilerExtensionVersion = Config.Android.composeVersion
  }
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:_")
  implementation("com.jakewharton.timber:timber:_")
  implementation("com.android.billingclient:billing-ktx:_")
  api("com.google.dagger:hilt-android:_")
  kapt("com.google.dagger:hilt-android-compiler:_")
}

kapt {
  correctErrorTypes = true
}
