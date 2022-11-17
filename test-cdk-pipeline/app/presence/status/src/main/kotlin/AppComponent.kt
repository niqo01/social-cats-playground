package com.nicolasmilliard.testcdkpipeline.presence

import aws.sdk.kotlin.runtime.auth.credentials.EnvironmentCredentialsProvider
import aws.sdk.kotlin.runtime.endpoint.AwsEndpoint
import aws.sdk.kotlin.runtime.endpoint.AwsEndpointResolver
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger
import software.amazon.lambda.powertools.metrics.MetricsUtils

interface AppComponent {
    val metricsLogger: MetricsLogger
    val tableName: String
    fun getDynamoDbClient(): DynamoDbClient
}
class ProdAppComponent: AppComponent {
    override val metricsLogger: MetricsLogger = MetricsUtils.metricsLogger()
    override val tableName = System.getenv("DDB_TABLE_NAME")

    override fun getDynamoDbClient(): DynamoDbClient {
        return DynamoDbClient{
            region = System.getenv("AWS_REGION")
            credentialsProvider = EnvironmentCredentialsProvider()
            endpointResolver = AwsEndpointResolver { service, region ->  AwsEndpoint("https://$service.$region.amazonaws.com") }
        }
    }
}