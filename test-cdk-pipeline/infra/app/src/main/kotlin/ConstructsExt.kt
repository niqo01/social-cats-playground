package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.Environment
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.pipelines.CodePipelineSource
import software.amazon.awscdk.pipelines.ConnectionSourceOptions
import software.amazon.awscdk.services.dynamodb.Table
import software.constructs.Construct
import java.util.*

fun Construct.setupStacks(envName: String, isProd: Boolean, lambdaArtifacts: Properties): SetupTasksResult {
    val dbStack = DbStack(
        this, "DbStack", object : DbStackProps {
            override val removalPolicy: RemovalPolicy
                get() = if (isProd) RemovalPolicy.RETAIN else RemovalPolicy.DESTROY

            override fun getTags(): Map<String, String> {
                return mapOf(
                    "testcdkpipeline:environment-type" to envName,
                    "testcdkpipeline:application-id" to "testpipepline-appid",
                )
            }

            override fun getTerminationProtection(): Boolean {
                return isProd
            }

            override fun getDescription(): String {
                return "Database Stack"
            }
        }

    )

    val apiStack = ApiStack(this, "ApiStack", object : ApiStackProps {
        override val table: Table
            get() = dbStack.table
        override val lambdaArtifacts: Properties
            get() = lambdaArtifacts

        override fun getTags(): Map<String, String> {
            return mapOf(
                "testcdkpipeline:environment-type" to envName,
                "testcdkpipeline:application-id" to "testpipepline-appid",
            )
        }

        override fun getDescription(): String {
            return "Api Stack"
        }
    })

    return SetupTasksResult(dbStack, apiStack, apiStack.apiUrlOutput)
}

data class SetupTasksResult(
    val dbStack: DbStack,
    val apiStack: ApiStack,
    val apiUrlOutput: CfnOutput
)

fun Construct.setupPipeline(lambdaArtifacts: Properties) {
    val githubConnection = CodePipelineSource.connection(
        "niqo01/social-cats-playground",
        "release/pipeline",
        ConnectionSourceOptions.Builder()
            .connectionArn("arn:aws:codestar-connections:us-east-1:480917579245:connection/11bca31c-2fcc-44c9-89b8-3a9e9c2f8df7")
            .triggerOnPush(true)
            .build()
    )
    val deploymentEnv = Environment.builder()
        .account("480917579245")
        .region("us-east-1")
        .build()

    val preProdEnv = Environment.builder()
        .account("480465344025")
        .region("us-east-1")
        .build()

    val prodEnv = Environment.builder()
        .account("275972720939")
        .region("us-east-1")
        .build()

    PipelineStack(
        this, "PipelineStack", object : PipelineStackProps {
            override val sourceCode: CodePipelineSource
                get() = githubConnection
            override val preProd: Environment
                get() = preProdEnv
            override val prod: Environment
                get() = prodEnv

            override fun getEnv() = deploymentEnv

            override fun getDescription()= "Pipeline App deployment to preprod and prod."

        },
        lambdaArtifacts
    )
}