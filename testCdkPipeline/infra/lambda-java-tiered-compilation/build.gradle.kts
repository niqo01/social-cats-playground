plugins {
    distribution
    id("maven-publish")
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
    repositories {
        maven {
            url =
                uri("https://test-domain-480917579245.d.codeartifact.us-east-1.amazonaws.com/maven/test-repository/")
            credentials {
                username = "aws"
                password = System.getenv("CODEARTIFACT_AUTH_TOKEN")
            }
        }
    }
}
