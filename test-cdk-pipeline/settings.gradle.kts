enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "test-cdk-pipeline"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven(url = "https://s3-us-west-2.amazonaws.com/dynamodb-local/release")
        maven {
            url = uri("https://test-domain-480917579245.d.codeartifact.us-east-1.amazonaws.com/maven/test-repository/")
            credentials {
                username = "aws"
                password = System.getenv("CODEARTIFACT_AUTH_TOKEN")
            }
        }
    }
}



include(":infra:app")
include(":infra:initial-setup")
include(":infra:integration-tests")
include(":infra:lambda-java-tiered-compilation")

include(":app:api")
include(":app:presence:status")

