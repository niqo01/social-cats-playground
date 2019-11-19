import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    extra.apply {
        set("isCiBuild", System.getenv("CI") == "true")
        set("gCloudServiceKey", System.getenv("GCLOUD_SERVICE_KEY"))
    }

    dependencies {
        classpath(Config.Plugins.googleServices)
        classpath(Config.Plugins.fabric)
        classpath(Config.Plugins.firebasePerformance)
        classpath(Config.Plugins.android)
        classpath(Config.Plugins.appDistribution)
    }
}

plugins {
    kotlin("jvm") apply false
    id("androidx.navigation.safeargs.kotlin") apply false
    id("com.google.android.gms.oss-licenses-plugin") apply false
    id("com.github.ben-manes.versions")
    id("org.jlleitschuh.gradle.ktlint")
}

allprojects {

    version = Config.Versions.name

    repositories {
        maven("https://kotlin.bintray.com/ktor")
        maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        google()
        mavenCentral()
        jcenter()
    }

    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    tasks.withType(org.jetbrains.kotlin.gradle.dsl.KotlinCompile::class).configureEach {
        kotlinOptions {
            freeCompilerArgs += listOf(
                "-progressive",
                "-Xnew-inference",
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xuse-experimental=kotlinx.coroutines.FlowPreview"
            )
        }
    }

    if (project.hasProperty("android")) {
        setKotlinCompile18(project)
    }

    plugins.withType(com.android.build.gradle.LibraryPlugin::class).configureEach {
        setBaseAndroidConfiguration(extensions.getByName("android") as BaseExtension)
    }
    plugins.withType(com.android.build.gradle.AppPlugin::class).configureEach {
        setBaseAndroidConfiguration(extensions.getByName("android") as BaseExtension)
    }
    plugins.withType(com.android.build.gradle.DynamicFeaturePlugin::class).configureEach {
        setBaseAndroidConfiguration(extensions.getByName("android") as BaseExtension)
    }
}



subprojects {
    val isReleaseBuild = !(version as String).endsWith("-SNAPSHOT")

    afterEvaluate {
        if (hasProperty("android")) {
            tasks.withType<KotlinCompile> {
                kotlinOptions.jvmTarget = "1.8"
            }
        }

        configure<PublishingExtension> {
            repositories {
                //            maven {
//                name = "GitHubPackages"
//                url = uri("https://maven.pkg.github.com/niqo01/social-cats-playground")
//                credentials {
//                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
//                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
//                }
//            }
                maven {
                    name = "Google Cloud Storage"

                    url = uri(
                        if (isReleaseBuild)
                            "https://storage.googleapis.com/repo-socialcats.milliard.page/snapshots"
                        else
                            "https://storage.googleapis.com/repo-socialcats.milliard.page/releases"
                    )

                    credentials {
                        username = findProperty("gstorage.key") as String? ?: System.getenv("G_STORAGE_USER")
                        password = findProperty("gstorage.password") as String? ?: System.getenv("G_STORAGE_PASSWORD")
                    }
                }
            }
            publications {
                when {
                    plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") -> {
                        logger.error("NICO android multiplatform plugin $name")
                        // TODO Implement
                    }
                    plugins.hasPlugin("com.android.library") -> {
                        logger.error("Create MavenPublication for Android library $name : Components size: ${components.size}")
                        create<MavenPublication>("release") {
                            groupId = Config.group
                            artifactId = "$rootProject-$name"
                            version = project.version.toString()

                            from(components["release"])
                        }

                        create<MavenPublication>("debug") {
                            groupId = Config.group
                            artifactId = "$rootProject-$name-debug"
                            version = project.version.toString()

                            from(components["debug"])
                        }
                    }
                    plugins.hasPlugin("java") -> {
                        logger.error("Create Maven Publication Java $name")
                        create<MavenPublication>("maven") {
                            groupId = Config.group
                            artifactId = "$rootProject-$name"
                            version = project.version.toString()

                            from(components["java"])
                        }
                    }
                }
            }

        }
    }
}

ktlint {
    version.set(Config.Plugins.ktlintVersion)
}

fun setKotlinCompile18(project: Project) {
    project.tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

fun setBaseAndroidConfiguration(extension: BaseExtension) {
    extension.apply {
        compileSdkVersion(Config.Android.SdkVersions.compile)

        buildToolsVersion = Config.Android.buildToolsVersion

        defaultConfig {
            minSdkVersion(Config.Android.SdkVersions.min)
            targetSdkVersion(Config.Android.SdkVersions.target)
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        viewBinding {
            isEnabled = true
        }

        lintOptions {
            textReport = true
            textOutput("stdout")
            setLintConfig(rootProject.file("lint.xml"))

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
}

apply(from = "gradle/projectDependencyGraph.gradle")
