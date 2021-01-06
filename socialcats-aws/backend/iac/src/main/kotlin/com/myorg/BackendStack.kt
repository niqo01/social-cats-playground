package com.myorg

import org.jetbrains.annotations.NotNull
import software.amazon.awscdk.core.*
import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.services.apigatewayv2.*
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegration
import software.amazon.awscdk.services.dynamodb.Attribute
import software.amazon.awscdk.services.dynamodb.AttributeType
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.dynamodb.TableProps
import software.amazon.awscdk.services.lambda.*
import software.amazon.awscdk.services.logs.LogGroup
import software.amazon.awscdk.services.logs.LogGroupProps
import software.amazon.awscdk.services.logs.RetentionDays
import java.io.File
import java.io.FileInputStream
import java.util.*


typealias LambdaFunction = software.amazon.awscdk.services.lambda.Function

class BackendStack @JvmOverloads constructor(
    scope: Construct?,
    id: String?,
    props: StackProps? = null,
    functionsProp:Properties
) :
    Stack(scope, id, props) {

    init {
        val dynamodbTable = createDynamoTable()

        val lambdaEnvMap = mapOf("TABLE_NAME" to dynamodbTable.tableName, "PRIMARY_KEY" to "itemId")

        val getItemFunction = createGetItemFunction(functionsProp.getProperty("getItem"),lambdaEnvMap)

        dynamodbTable.grantReadWriteData(getItemFunction);

        val api = createHttpApi()

        val getItemInt = LambdaProxyIntegration.Builder.create()
            .handler(getItemFunction)
            .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0).build()

        api.addRoutes(
            AddRoutesOptions.builder()
                .methods(listOf(HttpMethod.ANY))
                .path("/getItem")
                .integration(getItemInt)
                .build()
        )

    }

    private fun createHttpApi(): HttpApi {
        val api = HttpApi.Builder.create(this, "HttpApi")
            .apiName("Social cats API")
            .build()

        api.addStage(
            "BetaStage",
            HttpStageOptions.builder()
                .autoDeploy(true)
                .stageName("beta")
                .build()
        )

        LogGroup(
            this, "HttpApiLogGroup", LogGroupProps.builder()
                .retention(RetentionDays.ONE_WEEK)
                .logGroupName("httpapi-log-group")
                .build()
        )
        // Access logging not supported yet https://github.com/aws/aws-cdk/issues/11100
        // Enable manually https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-logging.html#http-api-enable-logging.console
        return api
    }

    private fun createGetItemFunction(
        asset: String,
        lambdaEnvMap: Map<String, @NotNull String>
    ): LambdaFunction {
        val func = LambdaFunction(
            this,
            "HandlerFunction",
            FunctionProps.builder()
                .code(Code.fromAsset(asset))
                .handler("com.nicolasmilliard.socialcatsaws.backend.functions.GetItemHandler")
                .runtime(Runtime.JAVA_11)
                .description("1.0-SNAPSHOT")
                .environment(lambdaEnvMap)
                .timeout(Duration.seconds(30))
                .memorySize(512)
                .tracing(Tracing.ACTIVE)
                .currentVersionOptions(
                    VersionOptions.builder().removalPolicy(RemovalPolicy.DESTROY).build()
                )
                .build()
        )
        func.currentVersion
        return func
    }

    private fun createDynamoTable(): Table {
        val partitionKey: Attribute = Attribute.builder()
            .name("itemId")
            .type(AttributeType.STRING)
            .build()
        val tableProps: TableProps = TableProps.builder()
            .tableName("items")
            .partitionKey(partitionKey)
            // The default removal policy is RETAIN, which means that cdk destroy will not attempt to delete
            // the new table, and it will remain in your account until manually deleted. By setting the policy to
            // DESTROY, cdk destroy will delete the table (even if it has data in it)
            .removalPolicy(RemovalPolicy.DESTROY)
            .build();
        return Table(this, "items", tableProps)
    }

}