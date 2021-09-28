package com.nicolasmilliard.testcdkpipeline

import com.amazonaws.xray.interceptors.TracingInterceptor
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.core.SdkSystemSetting
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger
import software.amazon.lambda.powertools.metrics.MetricsUtils
import java.net.URI

interface AppComponent {
    val metricsLogger: MetricsLogger
    val tableName: String
    fun getDynamoDbClient(): DynamoDbClient
}
class ProdAppComponent: AppComponent {
    override val metricsLogger: MetricsLogger = MetricsUtils.metricsLogger()
    override val tableName = System.getenv("DDB_TABLE_NAME")

    override fun getDynamoDbClient(): DynamoDbClient {
        val region = Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable()))
        return DynamoDbClient.builder()
            .httpClient(UrlConnectionHttpClient.builder().build())
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .region(region)
            .overrideConfiguration(
                ClientOverrideConfiguration.builder()
                    .addExecutionInterceptor(TracingInterceptor())
                    .build()
            )
            .endpointOverride(URI("https://dynamodb.$region.amazonaws.com"))
            .build()
    }
}