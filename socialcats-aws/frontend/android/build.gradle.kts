plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.kapt")
  id("dagger.hilt.android.plugin")
}

apply(from = "android.gradle")
apply(plugin = "app.cash.exhaustive")

dependencies {
  implementation(project(":feature:auth:android"))
  implementation(project(":feature:image-processing:android"))

  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:_")

  implementation(Kotlin.stdlib)
  implementation(KotlinX.coroutines)

  implementation("com.jakewharton.timber:timber:_")
  implementation(AndroidX.core.ktx)
  implementation(AndroidX.lifecycle.runtimeKtx)

  implementation(AndroidX.compose.ui)
  implementation(AndroidX.compose.material)
  implementation("androidx.compose.material:material-icons-extended:_")
  implementation("androidx.compose.ui:ui-tooling:_")
  implementation("androidx.navigation:navigation-compose:1.0.0-alpha07")
  implementation("androidx.navigation:navigation-runtime-ktx:_")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha01")
  implementation("androidx.activity:activity-compose:_")

  implementation("com.google.dagger:hilt-android:_")
  kapt("com.google.dagger:hilt-android-compiler:_")
  implementation("androidx.hilt:hilt-lifecycle-viewmodel:_")
  kapt("androidx.hilt:hilt-compiler:_")
  implementation("androidx.hilt:hilt-navigation:_")

  implementation("com.amplifyframework:core:_")
  implementation("androidx.work:work-runtime-ktx:_")

  implementation("com.squareup.okhttp3:logging-interceptor:_")

  implementation("androidx.startup:startup-runtime:_")

  implementation("dev.chrisbanes.accompanist:accompanist-coil:_")
  implementation("io.coil-kt:coil:_")

  testImplementation("junit:junit:_")

  androidTestImplementation(AndroidX.test.ext.junitKtx)
  androidTestImplementation(AndroidX.test.espresso.core)
}
