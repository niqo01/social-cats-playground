package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.*
import software.amazon.awscdk.pipelines.CdkPipeline
import software.amazon.awscdk.pipelines.CodePipeline
import software.amazon.awscdk.pipelines.CodePipelineSource
import software.amazon.awscdk.pipelines.ConnectionSourceOptions
import software.amazon.awscdk.pipelines.ShellStep
import software.amazon.awscdk.services.codepipeline.Artifact
import software.amazon.awscdk.services.codepipeline.Pipeline
import software.amazon.awscdk.services.dynamodb.BillingMode
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.dynamodb.TableProps
import software.constructs.Construct

class PipelineStack(scope: Construct, id: String, props: StackProps) : Stack(scope, id, props) {

    init {
        val pipeline = CodePipeline.Builder.create(this, "Pipeline")
            .synth(
                ShellStep.Builder.create("ShellStep")
                    .input(
                        CodePipelineSource.connection(
                            "", "", ConnectionSourceOptions.Builder()
                                .connectionArn("arn:aws:codestar-connections:us-east-1:480917579245:connection/11bca31c-2fcc-44c9-89b8-3a9e9c2f8df7")
                                .build()
                        )
                    )
                    .commands(
                        listOf(
                            "./testCdkPipeline/gradlew :infra:app:build",
                            "cd ./testCdkPipeline/infra/app/ && npx cdk synth"
                        )
                    ).installCommands(listOf(
                        "npm install -g aws-cdk"
                    ))
                    .build()
            )
            .build()

        pipeline.addStage(AppStage(this, "Preprod", StageProps.builder()
            .env(Environment.builder()
                .account("480465344025")
                .region("us-east-1")
                .build())
            .build()))
    }
}

class AppStage(scope: Construct, id: String, props: StageProps): Stage(scope, id, props){

    init {
        DbStack(this, "DbStack")
    }
}

