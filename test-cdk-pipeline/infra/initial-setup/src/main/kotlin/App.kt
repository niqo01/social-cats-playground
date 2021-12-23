package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.App
import software.amazon.awscdk.Environment
import software.amazon.awscdk.StackProps

const val ciAccount = "480917579245"
const val region = "us-east-1"

fun main() {
    val app = App()

    ArtifactRepositoryStack(app, "ArtifactRepositoryStack", StackProps.builder()
        .env(Environment.builder()
            .account(ciAccount)
            .region(region)
            .build())
        .build())

    app.synth()
}
