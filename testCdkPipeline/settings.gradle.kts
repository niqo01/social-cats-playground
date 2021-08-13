enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "test-cdk-pipeline"

include(":infra:app")
include(":infra:initial-setup")
include(":infra:integration-tests")

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "1.5.21"
    }
}

