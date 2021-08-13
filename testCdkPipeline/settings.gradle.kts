enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "test-cdk-pipeline"

include(":infra:app")
include(":infra:initial-setup")

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "1.5.21"
    }
}

