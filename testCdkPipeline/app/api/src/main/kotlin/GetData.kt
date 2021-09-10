package com.nicolasmilliard.testcdkpipeline

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import mu.KotlinLogging
import okio.ByteString.Companion.encodeUtf8
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.Select
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger
import software.amazon.lambda.powertools.logging.Logging
import software.amazon.lambda.powertools.metrics.Metrics
import software.amazon.lambda.powertools.tracing.Tracing

typealias MUnit = software.amazon.cloudwatchlogs.emf.model.Unit

private val log = KotlinLogging.logger {}

class GetData(appComponent: AppComponent = ProdAppComponent()) :
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private val metricsLogger: MetricsLogger = appComponent.metricsLogger
    private val tableName = appComponent.tableName
    private val dbClient: DynamoDbClient = appComponent.getDynamoDbClient()

    @Logging(logEvent = true)
    @Tracing
    @Metrics(captureColdStart = true)
    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        log.info { "Test" }
        metricsLogger.putMetric("GetDataCount", 1.0, MUnit.COUNT)

        val request = ScanRequest.builder()
            .tableName(tableName)
            .select(Select.COUNT)
            .build()
        val result = dbClient.scan(request)
        val count = result.count()
        val body = """
               {
                    "name": "Hello World $count"
                }
            """.trimIndent()
        val etag = body.encodeUtf8().md5().hex()

        if (input.headers["If-None-Match"] == etag) {
            return APIGatewayProxyResponseEvent()
                .withStatusCode(304)
        }

        return APIGatewayProxyResponseEvent()
            .withStatusCode(200)
            .withHeaders(
                mapOf(
                    "Content-Type" to "application/json",
                    "Cache-Control" to "public, max-age=30, must-revalidate",
                    "ETag" to etag,
                )
            )
            .withBody(body)
    }
}