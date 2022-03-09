import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
}

group = "me.niqo"
version = "1.0"

kotlin {
    jvm {
        val kotlinOptions: Map<String, String> by extra
        compilations.all {
            this.kotlinOptions.jvmTarget = kotlinOptions["jvmTarget"]!!
        }

        val javaOptions: Map<String,Any> by extra
        java.sourceCompatibility = javaOptions["sourceCompatibility"] as JavaVersion
        java.targetCompatibility = javaOptions["targetCompatibility"] as JavaVersion
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(projects.common)
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        javaHome = System.getenv("JAVA_HOME")
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "social-cats"
            packageVersion = "1.0.0"
        }
    }
}