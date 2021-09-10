package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.Environment
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.dynamodb.Table
import software.constructs.Construct
import java.util.*

fun Construct.setupStacks(envName: String, isProd: Boolean, lambdaArtifacts: Properties): Output {
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

    return Output(apiStack.apiUrlOutput)
}

fun Construct.setupPipeline(lambdaArtifacts: Properties) {
    PipelineStack(
        this, "PipelineStack", StackProps.builder()
            .env(
                Environment.builder()
                    .account("480917579245")
                    .region("us-east-1")
                    .build()
            )
            .build(),
        lambdaArtifacts
    )
}

data class Output(
    val apiUrlOutput: CfnOutput
)