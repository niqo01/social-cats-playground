package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.*
import software.amazon.awscdk.pipelines.*
import software.amazon.awscdk.services.codepipeline.Artifact
import software.amazon.awscdk.services.codepipeline.Pipeline
import software.amazon.awscdk.services.dynamodb.BillingMode
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.dynamodb.TableProps
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.PolicyStatement
import software.constructs.Construct

class PipelineStack(scope: Construct, id: String, props: StackProps) : Stack(scope, id, props) {

    init {

        val githubConnection = CodePipelineSource.connection(
            "niqo01/social-cats-playground",
            "nm/testCdkPipeline",
            ConnectionSourceOptions.Builder()
                .connectionArn("arn:aws:codestar-connections:us-east-1:480917579245:connection/11bca31c-2fcc-44c9-89b8-3a9e9c2f8df7")
                .triggerOnPush(true)
                .build()
        )

        val pipeline = CodePipeline.Builder.create(this, "Pipeline")
            .synth(
                CodeBuildStep.Builder.create("BuildStep")
                    .input(githubConnection)
                    .installCommands(
                        listOf(
                            "npm install -g aws-cdk@2.0.0-rc.17 cdk-assume-role-credential-plugin@1.4.0"
                        )
                    )
                    .commands(
                        listOf(
                            "cd ./testCdkPipeline/infra/app && npx cdk synth"
                        )
                    )
                    .primaryOutputDirectory("./testCdkPipeline/infra/app/cdk.out")
                    .rolePolicyStatements(
                        listOf(
                            PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .actions(listOf("sts:AssumeRole"))
                                .resources(listOf("arn:aws:iam::*:role/cdk-readOnlyRole"))
                                .build()
                        )
                    )
                    .build()
            )
            .crossAccountKeys(true)
            .build()

        val preProdStage = AppStage(
            this, "Preprod", StageProps.builder()
                .env(
                    Environment.builder()
                        .account("480465344025")
                        .region("us-east-1")
                        .build()
                )
                .build()
        )
        val preProdStageDeployment = pipeline.addStage(preProdStage)

        // Integration tests
        preProdStageDeployment.addPost(
            CodeBuildStep.Builder.create("IntegrationTestStep")
                .envFromCfnOutputs(mapOf("TABLE_NAME" to preProdStage.tableName))
                .input(githubConnection)
                .commands(
                    listOf(
                        "cd testCdkPipeline",
                        "./gradlew :infra:integration-tests:run"
                    )
                )
                .build()
        )

        val prodStage = pipeline.addStage(
            AppStage(
                this, "Prod", StageProps.builder()
                    .env(
                        Environment.builder()
                            .account("275972720939")
                            .region("us-east-1")
                            .build()
                    )
                    .build()
            )
        )

        prodStage.addPre(
            ManualApprovalStep.Builder.create("PromoteToProd")
                .comment("Promote to Prod?")
                .build()
        )
    }
}

class AppStage(scope: Construct, id: String, props: StageProps) : Stage(scope, id, props) {
    val tableName: CfnOutput

    init {
        val dbStack = DbStack(this, "DbStack")
        tableName = dbStack.tableName
    }
}

