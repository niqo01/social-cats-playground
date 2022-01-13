package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.Environment
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.StageProps
import software.amazon.awscdk.pipelines.*
import software.amazon.awscdk.services.codebuild.BuildSpec
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.PolicyStatement
import software.constructs.Construct
import java.util.*

class PipelineStack(scope: Construct, id: String, props: StackProps, lambdaArtifacts: Properties) :
    Stack(scope, id, props) {

    init {

        val githubConnection = CodePipelineSource.connection(
            "niqo01/social-cats-playground",
            "release/pipeline",
            ConnectionSourceOptions.Builder()
                .connectionArn("arn:aws:codestar-connections:us-east-1:480917579245:connection/11bca31c-2fcc-44c9-89b8-3a9e9c2f8df7")
                .triggerOnPush(true)
                .build()
        )

        val cliVersion = "2.14.0"
        val pipeline = CodePipeline.Builder.create(this, "Pipeline")
            .cliVersion(cliVersion)
            .synth(
                CodeBuildStep.Builder.create("BuildStep")
                    .input(githubConnection)
                    .installCommands(
                        listOf(
                            "npm install -g aws-cdk@$cliVersion",
                            "export CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token --domain test-domain --domain-owner 480917579245 --query authorizationToken --output text`"
                        )
                    )
                    .commands(
                        listOf(
                            "cd \$CODEBUILD_SRC_DIR/test-cdk-pipeline/infra/app && npx cdk synth"
                        )
                    )
                    .primaryOutputDirectory("\$CODEBUILD_SRC_DIR/test-cdk-pipeline/infra/app/cdk.out")
                    .rolePolicyStatements(
                        listOf(
                            PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .actions(listOf("sts:AssumeRole"))
                                .resources(listOf("arn:aws:iam::*:role/cdk-readOnlyRole"))
                                .build(),
                            PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .actions(
                                    listOf(
                                        "codeartifact:GetAuthorizationToken",
                                        "codeartifact:GetRepositoryEndpoint",
                                        "codeartifact:ReadFromRepository"
                                    )
                                )
                                .resources(listOf("*"))
                                .build(),
                            PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .actions(
                                    listOf(
                                        "sts:GetServiceBearerToken"
                                    )
                                )
                                .resources(listOf("*"))
                                .conditions(mapOf("StringEquals" to mapOf("sts:AWSServiceName" to "codeartifact.amazonaws.com")))
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
                .build(),
            "preprod",
            false,
            lambdaArtifacts
        )
        val preProdStageDeployment = pipeline.addStage(preProdStage)
//        ,AddStageOpts.builder()
//                .stackSteps(
//                    listOf(
//                        StackSteps.builder()
//                            .stack(preProdStage.result.dbStack)
//                            .changeSet(listOf(ManualApprovalStep.Builder.create("DB stack ChangeSet Approval").build()))
//                            .build(),
//                        StackSteps.builder()
//                            .stack(preProdStage.result.apiStack)
//                            .changeSet(
//                                listOf(
//                                    ManualApprovalStep.Builder.create("API stack ChangeSet Approval").build()
//                                )
//                            )
//                            .build()
//                    )
//                )
//                .build()
//        )

        // Integration tests
        preProdStageDeployment.addPost(
            CodeBuildStep.Builder.create("IntegrationTestStep")
                .envFromCfnOutputs(mapOf("API_URL" to preProdStage.result.apiUrlOutput))
                .input(githubConnection)
                .commands(
                    listOf(
                        "cd \$CODEBUILD_SRC_DIR/test-cdk-pipeline",
                        "./gradlew :infra:integration-tests:test"
                    )
                )
                .partialBuildSpec(
                    BuildSpec.fromObject(
                        mapOf(
                            "reports" to mapOf(
                                "testReport" to mapOf(
                                    "files" to "**/*",
                                    "base-directory" to "\$CODEBUILD_SRC_DIR/test-cdk-pipeline/infra/integration-tests/build/test-results/test",
                                    "file-format" to "JUNITXML"
                                )
                            )
                        )
                    )
                )
                .build()
        )

        val prodStage = AppStage(
            this, "Prod", StageProps.builder()
                .env(
                    Environment.builder()
                        .account("275972720939")
                        .region("us-east-1")
                        .build()
                )
                .build(),
            "prod",
            true,
            lambdaArtifacts
        )

        val prodStageDeployment = pipeline.addStage(
            prodStage, AddStageOpts.builder()
                .stackSteps(
                    listOf(
                        StackSteps.builder()
                            .stack(prodStage.result.dbStack)
                            .changeSet(listOf(ManualApprovalStep.Builder.create("DB stack ChangeSet Approval").build()))
                            .build(),
                        StackSteps.builder()
                            .stack(prodStage.result.apiStack)
                            .changeSet(
                                listOf(
                                    ManualApprovalStep.Builder.create("API stack ChangeSet Approval").build()
                                )
                            )
                            .build()
                    )
                )
                .build()
        )

        prodStageDeployment.addPre(
            ManualApprovalStep.Builder.create("PromoteToProd")
                .comment("Promote to Prod?")
                .build()
        )
    }
}
