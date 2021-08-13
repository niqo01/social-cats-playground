package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.App
import software.amazon.awscdk.Environment
import software.amazon.awscdk.StackProps


fun main() {
    val app = App()

    PipelineStack(
        app, "PipelineStack", StackProps.builder()
            .env(
                Environment.builder()
                    .account("480917579245")
                    .region("us-east-1")
                    .build()
            )
            .build()
    )

    app.synth()
}
