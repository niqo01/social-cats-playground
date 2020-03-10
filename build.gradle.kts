import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    extra.apply {
        set("isCiBuild", System.getenv("CI") == "true")
        set("gCloudServiceKey", System.getenv("GCLOUD_SERVICE_KEY"))
    }

    dependencies {
        classpath(Plugins.OldWay.googleServices)
        classpath(Plugins.OldWay.fabric)
        classpath(Plugins.OldWay.firebasePerformance)
        classpath(Plugins.OldWay.android)
        classpath(Plugins.OldWay.appDistribution)
    }
}

plugins {
    kotlin("jvm") apply false
    id(Plugins.Ids.KTLINT_GRADLE)
    id(Plugins.Ids.GRADLE_VERSION_PLUGIN)
    id(Plugins.Ids.SAFE_ARGS) apply false
    id(Plugins.Ids.ANDROID_OSS_LICENSES) apply false
//    id("com.autonomousapps.dependency-analysis") version "0.18.0"
}

allprojects {

    version = Config.Modules.Versions.name

    repositories {
        maven("https://kotlin.bintray.com/ktor")
        maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
        google()
        mavenCentral()
        jcenter()
    }

    apply(plugin = Plugins.Ids.MAVEN_PUBLISH)
    configurePublish()
}

subprojects {
    apply(plugin = Plugins.Ids.KTLINT_GRADLE)

    this.tasks.withType(org.jetbrains.kotlin.gradle.dsl.KotlinCompile::class).configureEach {
        kotlinOptions {
            freeCompilerArgs += listOf(
                "-progressive",
                "-Xnew-inference",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=kotlinx.coroutines.FlowPreview",
                "-Xopt-in=kotlin.RequiresOptIn"
            )
        }
    }

    configureAndroidPlugins()
    configureJavaPlugins()

    afterEvaluate {
        // If project has an Android plugin, set Kotlin compile to 1.8
        ANDROID_PLUGIN_IDS.forEach {
            if (plugins.hasPlugin(it)) {
                setKotlinCompileTarget()
                return@afterEvaluate
            }
        }

        configureAndroidPublishMultiplatformPlugins()
    }
}

ktlint {
    version.set(Plugins.Versions.ktlint)
}

fun Project.setKotlinCompileTarget() {
    project.tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = Config.Android.kotlinJvmTarget
    }
}

fun Project.configureJavaPlugins() {
    plugins.withType(JavaPlugin::class).configureEach {
        extensions.getByType(JavaPluginExtension::class).apply {
            withSourcesJar()
        }
    }
}

fun Project.configureAndroidPublishMultiplatformPlugins(){
    plugins.withId(Plugins.Ids.KOTLIN_MULTIPLATFORM) {
        extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class).apply {
            val androidTarget = this.targets.asMap["android"]

            if (androidTarget != null){
                (androidTarget as org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget).publishAllLibraryVariants()
            }
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
    }

    compileOptions {
        sourceCompatibility = Config.Android.sourceCompatibility
        targetCompatibility = Config.Android.targetCompatibility
    }

    // replace with getBuildFeature() getter on BaseExtension when ready
    if (this is com.android.build.gradle.LibraryExtension) {
        buildFeatures {
            // Determines whether to enable support for Jetpack Compose.
            compose = false
            viewBinding = true
            dataBinding = false
            renderScript = false
            aidl = false
            shaders = false
        }
    }
    if (this is com.android.build.gradle.internal.dsl.BaseAppModuleExtension) {
        buildFeatures {
            // Determines whether to enable support for Jetpack Compose.
            compose = false
            viewBinding = true
            dataBinding = false
            renderScript = false
            aidl = false
            shaders = false
        }
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
