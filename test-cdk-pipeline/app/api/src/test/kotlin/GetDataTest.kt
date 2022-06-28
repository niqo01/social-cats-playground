package com.nicolasmilliard.testcdkpipeline

import app.cash.tempest2.testing.JvmDynamoDbServer
import app.cash.tempest2.testing.TestDynamoDb
import app.cash.tempest2.testing.TestTable
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.runtime.endpoint.AwsEndpoint
import aws.sdk.kotlin.runtime.endpoint.AwsEndpointResolver
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import com.amazonaws.services.lambda.runtime.tests.EventLoader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger
import software.amazon.lambda.powertools.metrics.MetricsUtils

const val PORT = 6001
const val TABLE_NAME = "test_items"

@OptIn(ExperimentalCoroutinesApi::class)
class GetDataTest {

    @RegisterExtension
    @JvmField
    val db = testDb()

    @Test
    fun test() = runTest {
        val event = EventLoader.loadApiGatewayRestEvent("apigw_rest_event.json")
        val getData = GetData(object : AppComponent {
            override val metricsLogger: MetricsLogger
                get() = MetricsUtils.metricsLogger()
            override val tableName: String
                get() = TABLE_NAME

            override fun getDynamoDbClient(): DynamoDbClient {
                return DynamoDbClient {
                    credentialsProvider = StaticCredentialsProvider {
                        accessKeyId = "key"
                        secretAccessKey = "secret"
                    }
                    region = "us-west-2"
                    endpointResolver = AwsEndpointResolver { _, _ -> AwsEndpoint("http://localhost:$PORT") }
                }

            }
        })

        val response = getData.handleRequest(event, FakeContext())

        assertThat(response.statusCode).isEqualTo(200)
        assertThat(response.body).contains("Hello World")
    }
}

fun testDb() = TestDynamoDb.Builder(JvmDynamoDbServer.Factory)
    .addTable(TestTable.create<TestItem>(TABLE_NAME))
    .port(PORT)
    .build()


@DynamoDbBean
class TestItem {
    @get:DynamoDbPartitionKey
    var pk: String? = null

    @get:DynamoDbSortKey
    var sk: String? = null
}
