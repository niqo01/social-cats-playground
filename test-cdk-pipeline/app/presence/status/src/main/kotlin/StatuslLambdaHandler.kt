package com.nicolasmilliard.testcdkpipeline.presence


import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger
import software.amazon.lambda.powertools.logging.Logging
import software.amazon.lambda.powertools.metrics.Metrics
import java.lang.IllegalArgumentException


typealias MUnit = software.amazon.cloudwatchlogs.emf.model.Unit

private val log = KotlinLogging.logger {}

class StatuslLambdaHandler( appComponent: AppComponent = ProdAppComponent()) :
    RequestHandler<Map<String, Object>, Presence> {

    private val metricsLogger: MetricsLogger = appComponent.metricsLogger
    private val jedis = appComponent.getJedisCluster()

    @Logging(logEvent = true)
    @Metrics(captureColdStart = true)
    override fun handleRequest(input: Map<String, Object>, context: Context): Presence =
        runBlocking {
            log.info { "Test" }
            metricsLogger.putMetric("StatusCount", 1.0, MUnit.COUNT)

            val arguments = input["arguments"] as Map<String, String>
            val id = arguments["id"] ?: throw IllegalArgumentException("Missing argument 'id'")
            return zscore(id)

        }

    suspend fun zscore(id: String): Presence {
        val result = jedis.zscore("presence", id)
        return Presence(id, if (result >0)Status.online else Status.offline)
    }
}

enum class Status{
    online, offline
}

data class Presence(
    val id: String,
    val status: Status
)