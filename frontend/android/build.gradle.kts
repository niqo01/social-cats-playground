

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.android.gms.oss-licenses-plugin")
    id("io.fabric")
    id("com.google.firebase.firebase-perf")
    id("com.google.firebase.appdistribution")
    `maven-publish`
}

android {

    val isCiBuild = rootProject.extra["isCiBuild"] as Boolean
    val gCloudServiceKey = rootProject.extra["gCloudServiceKey"] as String?

    defaultConfig {
        applicationId = "com.nicolasmilliard.socialcats"

        versionCode = Config.Versions.AndroidApp.code
        versionName = Config.Versions.AndroidApp.name

        resConfigs("en")

        buildConfigField("boolean", "IS_CI_BUILD", "false")
        buildConfigField("String", "COMMIT_SHA", "\"${gitSha()}\"")
        buildConfigField("long", "COMMIT_UNIX_TIMESTAMP", "${gitTimestamp()}L")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        if (file("upload.keystore").exists()) {
            create("upload") {
                storeFile = rootProject.file("upload.keystore")
                storePassword = System.getenv("UPLOAD_STORE_PASSWORD")
                keyAlias = "playground"
                keyPassword = System.getenv("UPLOAD_KEY_PASSWORD")
            }
        }
    }

    buildTypes {

        getByName("debug") {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("boolean", "IS_CI_BUILD", isCiBuild.toString())
        }

        getByName("release") {
            signingConfig = if (file("upload.keystore").exists()) {
                signingConfigs.getByName("upload")
            } else {
                signingConfigs.getByName("debug")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("src/main/shrinker-rules.pro")
            firebaseAppDistribution {
                groups = "internal"
                if (!gCloudServiceKey.isNullOrBlank()) {
                    serviceCredentialsFile = getGCloudKeyFilePath(gCloudServiceKey)
                }
            }
        }
    }

    packagingOptions {
        exclude("META-INF/atomicfu.kotlin_module")
        exclude("META-INF/kotlinx-coroutines-core.kotlin_module")
        exclude("META-INF/kotlinx-serialization-runtime.kotlin_module")
        exclude("META-INF/kotlin-logging.kotlin_module")
        exclude("META-INF/ui-android_debug.kotlin_module")
        exclude("META-INF/session_debug.kotlin_module")
        exclude("META-INF/ui-android_release.kotlin_module")
        exclude("META-INF/session_release.kotlin_module")
        exclude("META-INF/client_debug.kotlin_module")
        exclude("META-INF/client_release.kotlin_module")
        exclude("META-INF/search-presenter_debug.kotlin_module")
        exclude("META-INF/search-presenter_release.kotlin_module")
    }
}

dependencies {

    implementation(project(":frontend:android:base"))
    implementation(project(":search:ui-android"))
    implementation(project(":account:ui-android"))
    implementation(project(":cloud-messaging:android"))

    debugImplementation(Config.Libs.LeakCanary.leakCanary)
    releaseImplementation(Config.Libs.LeakCanary.leakCanaryNoop)
    debugImplementation(Config.Libs.LeakCanary.leakCanaryFragments)

    testImplementation(Config.Libs.Test.androidxJunit)
    testImplementation(Config.Libs.Test.androidxtruth)
    testImplementation(Config.Libs.Test.espressoCore)
    testImplementation(Config.Libs.Test.robolectric)
    testImplementation(Config.Libs.Kotlin.Coroutine.test)
    testImplementation(Config.Libs.AndroidX.pagingCommon)
    testImplementation(Config.Libs.AndroidX.fragmentTesting)
    testImplementation(Config.Libs.AndroidX.workTesting)
}

// Because the components are created only during the afterEvaluate phase, you must
// configure your publications using the afterEvaluate() lifecycle method.
afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/niqo01/social-cats-playground")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
                }
            }
        }
        publications {
            // Creates a Maven publication called "release".
            create<MavenPublication>("release") {
                // Applies the component for the release build variant.
                from(components.findByName("release_aab"))

                groupId = android.defaultConfig.applicationId
                artifactId = "android-app"
                version = Config.Versions.AndroidApp.name
            }
        }
    }
}

apply(plugin = "com.google.gms.google-services")
