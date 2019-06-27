import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.android.gms.oss-licenses-plugin")
    id("io.fabric")
    id("com.google.firebase.firebase-perf")
}

android {

    val isCiBuild = rootProject.extra["isCiBuild"] as Boolean

    compileSdkVersion(Config.Android.SdkVersions.compile)

    buildToolsVersion = Config.Android.buildToolsVersion
    defaultConfig {
        applicationId = "com.nicolasmilliard.socialcats"

        minSdkVersion(Config.Android.SdkVersions.min)
        targetSdkVersion(Config.Android.SdkVersions.target)

        versionCode = Config.Versions.AndroidApp.code
        versionName = Config.Versions.AndroidApp.name

        resConfigs("en")

        buildConfigField("boolean", "IS_CI_BUILD", "false")
        buildConfigField("String", "COMMIT_SHA", "\"${gitSha()}\"")

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
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lintOptions {
        textReport = true
        textOutput("stdout")
        setLintConfig(rootProject.file("lint.xml"))

        isCheckDependencies = true
        isCheckTestSources = true
        isExplainIssues = false

        // We run a full lint analysis as build part in CI, so skip vital checks for assemble task.
        isCheckReleaseBuilds = false

        testOptions {
            unitTests.isIncludeAndroidResources = true
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
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {

    implementation(project(":frontend:android:base"))
    implementation(project(":search:ui-android"))
    implementation(project(":account:ui-android"))
    implementation(project(":cloud-messaging:android"))

    debugImplementation(Config.Libs.LeakCanary.leakCanary)
    releaseImplementation(Config.Libs.LeakCanary.leakCanaryNoop)
    debugImplementation(Config.Libs.LeakCanary.leakCanaryFragments)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Config.Libs.Kotlin.jdk8)
    implementation(Config.Libs.Kotlin.Coroutine.android)

    implementation(Config.Libs.AndroidX.appCompat)
    implementation(Config.Libs.AndroidX.activityKtx)
    implementation(Config.Libs.AndroidX.fragmentKtx)
    implementation(Config.Libs.AndroidX.preferenceKtx)
    implementation(Config.Libs.AndroidX.coreKtx)
    implementation(Config.Libs.AndroidX.constraintLayout)
    implementation(Config.Libs.AndroidX.recyclerView)
    implementation(Config.Libs.AndroidX.viewModelKtx)
    implementation(Config.Libs.AndroidX.lifecycleKtx)
    implementation(Config.Libs.AndroidX.lifecycleCommon)
    implementation(Config.Libs.AndroidX.pagingRuntimeKtx)
    implementation(Config.Libs.AndroidX.dynamicAnimation)
    implementation(Config.Libs.AndroidX.navigationFragmentKtx)
    implementation(Config.Libs.AndroidX.navigationUiKtx)
    implementation(Config.Libs.AndroidX.workRuntimeKtx)

    implementation(Config.Libs.timber)
    implementation(Config.Libs.shimmer)

    testImplementation(Config.Libs.Test.androidxJunit)
    testImplementation(Config.Libs.Test.androidxtruth)
    testImplementation(Config.Libs.Test.espressoCore)
    testImplementation(Config.Libs.Test.robolectric)
    testImplementation(Config.Libs.Kotlin.Coroutine.test)
    testImplementation(Config.Libs.AndroidX.pagingCommon)
    testImplementation(Config.Libs.AndroidX.fragmentTesting)
    testImplementation(Config.Libs.AndroidX.workTesting)
}

fun gitSha(): String {
    val f = File(buildDir, "commit-sha.txt")
    if (!f.exists()) {
        val p = Runtime.getRuntime().exec("git rev-parse HEAD", null, project.rootDir)
        val input = p.inputStream.bufferedReader().use { it.readText().trim() }
        if (p.waitFor() != 0) {
            throw RuntimeException(p.errorStream.bufferedReader().use { it.readText() })
        }
        f.parentFile.mkdirs()
        f.writeText(input)
    }
    return f.readText()
}

apply(plugin = "com.google.gms.google-services")
