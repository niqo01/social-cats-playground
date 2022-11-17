plugins {
    distribution
    id("com.nicolasmilliard.publish")
}

distributions {
    main {
        distributionBaseName.set("layer")
        contents {
            from("src/resources")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.nicolasmilliard.testcdkpipeline"
            artifactId = "lambda-tiered-compilation-layer"
            version = "0.0.1"

            artifact(tasks.distZip.get())
        }
    }
}
