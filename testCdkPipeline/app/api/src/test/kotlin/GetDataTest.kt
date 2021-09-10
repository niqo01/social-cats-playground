package com.nicolasmilliard.testcdkpipeline

import app.cash.tempest2.testing.JvmDynamoDbServer
import app.cash.tempest2.testing.TestDynamoDb
import app.cash.tempest2.testing.TestTable
import com.amazonaws.services.lambda.runtime.tests.EventLoader
import com.amazonaws.xray.AWSXRay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger
import software.amazon.lambda.powertools.metrics.MetricsUtils

class GetDataTest {

    @RegisterExtension
    @JvmField
    val db = testDb()

    @Test
    fun test() {
        val event = EventLoader.loadApiGatewayRestEvent("apigw_rest_event.json")
        val getData = GetData(object : AppComponent {
            override val metricsLogger: MetricsLogger
                get() = MetricsUtils.metricsLogger()
            override val tableName: String
                get() = "test_items"

            override fun getDynamoDbClient(): DynamoDbClient = db.dynamoDb
        })

        val response = getData.handleRequest(event, FakeContext())

        assertThat(response.statusCode).isEqualTo(200)
        assertThat(response.body).contains("Hello World")
    }
}

fun testDb() = TestDynamoDb.Builder(JvmDynamoDbServer.Factory)
    .addTable(TestTable.create<TestItem>("test_items"))
    .build()


@DynamoDbBean
class TestItem {
    @get:DynamoDbPartitionKey
    var pk: String? = null

    @get:DynamoDbSortKey
    var sk: String? = null
}
