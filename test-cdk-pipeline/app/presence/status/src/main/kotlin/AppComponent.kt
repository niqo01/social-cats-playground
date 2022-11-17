package com.nicolasmilliard.testcdkpipeline.presence

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.Connection
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisCluster
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger
import software.amazon.lambda.powertools.metrics.MetricsUtils


interface AppComponent {
    val metricsLogger: MetricsLogger
    fun getJedisCluster(): JedisCluster
}
class ProdAppComponent: AppComponent {
    override val metricsLogger: MetricsLogger = MetricsUtils.metricsLogger()

    override suspend fun getJedisCluster(): JedisCluster {
        val hostAndPort = HostAndPort(System.getenv("ClusterAddress"), System.getenv("REDIS_PORT"))
        return JedisCluster(
            clusterNodes = setOf(hostAndPort),
            connectionTimeout = 5000,
            soTimeout = 5000,
            maxAttempts = 2,
            password = null,
            clientName=null,
            poolConfig = GenericObjectPoolConfig<Connection>(),
            ssl = true
        )
    }
}