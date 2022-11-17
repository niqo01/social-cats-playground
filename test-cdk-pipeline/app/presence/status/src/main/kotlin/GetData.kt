package com.nicolasmilliard.testcdkpipeline.presence

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.ScanRequest
import aws.sdk.kotlin.services.dynamodb.model.Select
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import okio.ByteString.Companion.encodeUtf8
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger
import software.amazon.lambda.powertools.logging.Logging
import software.amazon.lambda.powertools.metrics.Metrics

typealias MUnit = software.amazon.cloudwatchlogs.emf.model.Unit

private val log = KotlinLogging.logger {}

class GetData(appComponent: AppComponent = ProdAppComponent()) :
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private val metricsLogger: MetricsLogger = appComponent.metricsLogger
    private val tableName = appComponent.tableName
    private val dbClient: DynamoDbClient = appComponent.getDynamoDbClient()

    @Logging(logEvent = true)
    @Metrics(captureColdStart = true)
    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent =
        runBlocking {
            log.info { "Test" }
            metricsLogger.putMetric("GetDataCount", 1.0, MUnit.COUNT)

            val count = countItems()
            val json = """
               {
                    "name": "Hello World $count"
                }
            """.trimIndent()
            val etag = json.encodeUtf8().md5().hex()

            if (input.headers["If-None-Match"] == etag) {
                return@runBlocking APIGatewayProxyResponseEvent()
                    .withStatusCode(304)
            }

            return@runBlocking APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(
                    mapOf(
                        "Content-Type" to "application/json",
                        "Cache-Control" to "public, max-age=30, must-revalidate",
                        "ETag" to etag,
                    )
                )
                .withBody(json)
        }

    suspend fun countItems(): Int {
        val request = ScanRequest {
            tableName = this@GetData.tableName
            select = Select.Count
        }
        val result = dbClient.scan(request)
        return result.count
    }
}