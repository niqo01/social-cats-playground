plugins {
    `kotlin-dsl`
}

group = "com.nicolasmilliard.buildlogic"


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.spotless.gradlePlugin)
    implementation(libs.checkupdates.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kotlinApplication") {
            id = "com.nicolasmilliard.kotlin.application"
            implementationClass = "KotlinApplicationConventionPlugin"
        }
        register("publish") {
            id = "com.nicolasmilliard.publish"
            implementationClass = "PublishConventionPlugin"
        }
        register("spotless") {
            id = "com.nicolasmilliard.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
        register("checkUpdates") {
            id = "com.nicolasmilliard.check.updates"
            implementationClass = "CheckUpdatesConventionPlugin"
        }
    }
}