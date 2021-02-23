import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.nicolasmilliard.socialcats-aws"

buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:_")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:_")
        classpath("app.cash.exhaustive:exhaustive-gradle:_")
        classpath("com.google.dagger:hilt-android-gradle-plugin:_")
        classpath("com.guardsquare:proguard-gradle:_")
        classpath("com.google.gms:google-services:_")
    }
}

plugins {
    id("com.diffplug.spotless")
}

subprojects {

    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "app.cash.exhaustive")

    repositories {
        google()
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/kotlinx/")
        maven(url = "https://jitpack.io")
        maven(url = "https://s3-us-west-2.amazonaws.com/dynamodb-local/release")
    }

    spotless {
        kotlin {
            target("**/*.kt")
            ktlint("0.40.0").userData(
                mapOf(
                    "indent_size" to "2",
                    "continuation_indent_size" to "2"
                )
            )
        }
        kotlinGradle {
            ktlint("0.40.0").userData(
                mapOf(
                    "indent_size" to "2",
                    "continuation_indent_size" to "2"
                )
            )
        }
    }
    setKotlinCompileTarget()
    configureAndroidPlugins()
    configureJavaPlugins()

    afterEvaluate {
        configureAndroidPublishMultiplatformPlugins()
    }
}

fun Project.configureAndroidPublishMultiplatformPlugins() {
    plugins.withId(Plugins.Ids.KOTLIN_MULTIPLATFORM) {
        extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class).apply {
            val androidTarget = this.targets.asMap["android"]

            if (androidTarget != null) {
                (androidTarget as org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget).apply {
                    publishAllLibraryVariants()
                }
            }
        }
    }
}

fun Project.getAndroidPlugin(): String? {
    for (pluginId in ANDROID_PLUGIN_IDS) {
        if (this.plugins.hasPlugin(pluginId)) return pluginId
    }
    return null
}

fun Project.setKotlinCompileTarget() {
    tasks.withType<KotlinCompile> {
        sourceCompatibility = Config.Java.sourceCompatibility.toString()
        targetCompatibility = Config.Java.targetCompatibility.toString()
        kotlinOptions {
            jvmTarget = Config.Kotlin.jvmTarget
            freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.flow.ExperimentalCoroutinesApi"
            freeCompilerArgs += "-Xopt-in=kotlin.time.ExperimentalTime"
                freeCompilerArgs += "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
            if (!this@setKotlinCompileTarget.isApplication()) {
                freeCompilerArgs += "-Xexplicit-api=strict"
            }
            freeCompilerArgs += listOf(
                "-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
            )
            useIR = true
        }
    }
}

fun Project.isApplication(): Boolean {
    return path.startsWith(":frontend")
        || path.startsWith(":backend")
        || path.contains(":backend:functions")
}

fun Project.configureJavaPlugins() {
    plugins.withType(JavaPlugin::class).configureEach {
        extensions.getByType(JavaPluginExtension::class).apply {
            withSourcesJar()
        }
    }
}

fun Project.configureAndroidPlugins() {
    ANDROID_PLUGIN_IDS.forEach {
        plugins.withId(it) {
            extensions.getByType(BaseExtension::class).setBaseAndroidConfiguration()
        }
    }
}

fun BaseExtension.setBaseAndroidConfiguration() {
    compileSdkVersion(Config.Android.SdkVersions.compile)

    buildToolsVersion = Config.Android.buildToolsVersion

    defaultConfig {
        minSdkVersion(Config.Android.SdkVersions.min)
        targetSdkVersion(Config.Android.SdkVersions.target)
        versionCode = Config.Android.Versions.code
        versionName = Config.Android.Versions.name
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    compileOptions {
        sourceCompatibility = Config.Android.sourceCompatibility
        targetCompatibility = Config.Android.targetCompatibility
    }


    lintOptions {
        textReport = true
        textOutput("stdout")
        lintConfig = rootProject.file("lint.xml")

        isCheckDependencies = true
        isCheckGeneratedSources = true
        isCheckTestSources = true
        isExplainIssues = false

        // We run a full lint analysis as build part in CI, so skip vital checks for assemble task.
        isCheckReleaseBuilds = false

        testOptions {
            unitTests.isIncludeAndroidResources = true
        }
    }
}

apply(from = "gradle/projectDependencyGraph.gradle")
