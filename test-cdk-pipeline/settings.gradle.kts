enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "test-cdk-pipeline"

include(":infra:app")
include(":infra:initial-setup")
include(":infra:integration-tests")
include(":infra:lambda-java-tiered-compilation")

include(":app:api")
