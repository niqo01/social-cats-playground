package com.nicolasmilliard.socialcatsaws.backend.util

import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.Duration
import software.amazon.awscdk.core.RemovalPolicy
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.lambda.Code
import software.amazon.awscdk.services.lambda.FunctionProps
import software.amazon.awscdk.services.lambda.ILayerVersion
import software.amazon.awscdk.services.lambda.LayerVersion
import software.amazon.awscdk.services.lambda.Runtime
import software.amazon.awscdk.services.lambda.VersionOptions

fun getLambdaInsightPolicy(): PolicyStatement {
  return PolicyStatement.Builder.create().effect(Effect.ALLOW).actions(
    listOf(
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    )
  ).resources(listOf("arn:aws:logs:*:*:log-group:/aws/lambda-insights:*")).build()
}

private fun getCloudWatchLambdaInsightLayerVersion(construct: Construct, region: String, layerId: String): ILayerVersion {
  return LayerVersion.fromLayerVersionArn(
    construct,
    layerId,
    "arn:aws:lambda:$region:580247275435:layer:LambdaInsightsExtension:12"
  )
}

fun buildLambdaProps(
  construct: Construct,
  asset: String,
  region: String,
  handler: String,
  description: String,
  version: String,
  layerId: String,
  env: Map<String, String> = emptyMap()
): FunctionProps {
  val layerVersion =
    getCloudWatchLambdaInsightLayerVersion(construct, region, layerId)
  return FunctionProps.builder()
    .code(Code.fromAsset(asset))
    .handler(handler)
    .description(description)
    .runtime(Runtime.JAVA_11)
    .timeout(Duration.seconds(30))
    .memorySize(512)
    .layers(listOf(layerVersion))
    .environment(env)
    .currentVersionOptions(
      VersionOptions.builder()
        .description(version)
        .removalPolicy(RemovalPolicy.DESTROY)
        .build()
    )
    .build()
}
