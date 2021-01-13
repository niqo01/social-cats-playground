import de.fayard.refreshVersions.bootstrapRefreshVersions

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies.classpath("de.fayard.refreshVersions:refreshVersions:0.9.7")
}

bootstrapRefreshVersions()

rootProject.name = "Social Cats Aws"
include(":feature:auth:android")
include(":feature:auth:backend:functions:cognito-confirmation-dynamo")
include(":feature:profile:backend:models")
include(":feature:profile:backend:use-cases")
include(":feature:profile:backend:repository")
include(":feature:conversations:repository:admin")
include(":feature:image-processing:api")
include(":feature:image-processing:api:model")
include(":feature:image-processing:android")
include(":feature:image-processing:backend:use-cases")
include(":feature:image-processing:backend:functions:image-upload-url")
include(":feature:image-processing:backend:functions:image-upload-dynamo")
include(":frontend:android")
include(":backend:aws-cdk")

include(":library:sharp")
include(":library:cloud-metrics")
include(":library:image-object-store")
include(":library:object-store")


