package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.services.lambda.ILayerVersion
import software.amazon.awscdk.services.lambda.LayerVersion
import software.constructs.Construct

fun getCloudWatchLambdaInsightLayerVersion(construct: Construct, region: String, layerId: String): ILayerVersion {
    return LayerVersion.fromLayerVersionArn(
        construct,
        layerId,
        "arn:aws:lambda:$region:580247275435:layer:LambdaInsightsExtension:12"
    )
}