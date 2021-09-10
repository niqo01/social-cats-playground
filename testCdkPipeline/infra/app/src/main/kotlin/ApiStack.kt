package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.*
import software.amazon.awscdk.Stack
import software.amazon.awscdk.cloudformation.include.CfnInclude
import software.amazon.awscdk.services.apigateway.*
import software.amazon.awscdk.services.cloudfront.*
import software.amazon.awscdk.services.cloudfront.origins.HttpOrigin
import software.amazon.awscdk.services.cloudwatch.Alarm
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.iam.AnyPrincipal
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.ManagedPolicy
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.lambda.*
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.logs.LogGroup
import software.amazon.awscdk.services.logs.RetentionDays
import software.amazon.awscdk.services.s3.BlockPublicAccess
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.s3.BucketEncryption
import software.amazon.awscdk.services.s3.CfnBucket
import software.constructs.Construct
import java.util.*

class ApiStack(scope: Construct, id: String, props: ApiStackProps) :
    Stack(scope, id, props) {

    var apiUrlOutput: CfnOutput

    init {

        val optimizationLayer = createTieredCompilationLambdaLayer(props)

        val alias = createGetDataLambda(props, optimizationLayer)

        val apiKeyValue = "MyApiKeyThatIsAtLeast20Characterss"

        val prdLogGroup = LogGroup.Builder.create(this, "RestApiAccessLog")
            .retention(RetentionDays.ONE_MONTH)
            .build()

        val restApi = createRestApi(prdLogGroup, apiKeyValue)

        restApi.addLambdaResource(alias)

        val cloudFront = createCfDistribution(restApi, apiKeyValue)

        apiUrlOutput = CfnOutput(
            this, "CloudFrontDomainOutput", CfnOutputProps.builder()
                .value(cloudFront.domainName)
                .build()
        )
    }

    private fun RestApi.addLambdaResource(alias: Alias) {
        val getData = root.addResource("getData")
        getData.addMethod(
            "GET", LambdaIntegration.Builder.create(alias)
                .proxy(true)
                .build(),
            MethodOptions.builder()
                .apiKeyRequired(true)
                .build()
        )
    }

    private fun createCfDistribution(
        restApi: RestApi,
        apiKeyValue: String
    ): Distribution {
        val apiEndPointUrlWithoutProtocol = restApi.url.split("://")[1]
        val apiEndPointDomainName = apiEndPointUrlWithoutProtocol.split("/")[0]

        val cfBucket = createCloudFrontLoggingBucket()
        val wafTemplate = CfnInclude.Builder.create(this, "WafSecurityAutomation")
            .templateFile("src/main/resources/aws-waf-security-automations-v2020-06.template")
            .preserveLogicalIds(false)
            .parameters(
                mapOf(
                    "ActivateAWSManagedRulesParam" to "no",
                    "ActivateSqlInjectionProtectionParam" to "no",
                    "ActivateCrossSiteScriptingProtectionParam" to "no",
                    "ActivateHttpFloodProtectionParam" to "yes - AWS WAF rate based rule",
                    "ActivateScannersProbesProtectionParam" to "yes - Amazon Athena log parser",
                    "ActivateReputationListsProtectionParam" to "yes",
                    "ActivateBadBotProtectionParam" to "yes",
                    "EndpointType" to "CloudFront",
                    "AppAccessLogBucket" to cfBucket.bucketName,
                    "ErrorThreshold" to 20,
                    "RequestThreshold" to 100,
                    "WAFBlockPeriod" to 240,
                    "KeepDataInOriginalS3Location" to "No",
                )
            )
            .build()

        val webAclArn = wafTemplate.getOutput("WAFWebACLArn")

        val cloudFront = Distribution.Builder.create(this, "CloudFront")
            .comment("Cloud Front for API")
            .defaultBehavior(
                BehaviorOptions.builder()
                    .origin(
                        HttpOrigin.Builder.create(apiEndPointDomainName)
                            .originPath("/${restApi.deploymentStage.stageName}")
                            .originSslProtocols(listOf(OriginSslPolicy.TLS_V1_2))
                            .customHeaders(mapOf("x-api-key" to apiKeyValue))
                            .originShieldRegion(region)
                            .readTimeout(Duration.seconds(30))
                            .build()
                    )
                    .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                    .compress(true)
                    .cachePolicy(
                        CachePolicy.Builder.create(this, "CustomCachePolicy")
                            .comment("Custom Cache policy")
                            .queryStringBehavior(CacheQueryStringBehavior.all())
                            .headerBehavior(CacheHeaderBehavior.none())
                            .cookieBehavior(CacheCookieBehavior.none())
                            .minTtl(Duration.seconds(0))
                            .maxTtl(Duration.days(365))
                            .defaultTtl(Duration.seconds(0))
                            .enableAcceptEncodingGzip(true)
                            .enableAcceptEncodingBrotli(true)
                            .build()
                    )
                        // For some reason this triggers request to fails with 403
//                    .originRequestPolicy(
//                        OriginRequestPolicy.Builder.create(this, "CustomOriginPolicy")
//                            .cookieBehavior(OriginRequestCookieBehavior.all())
//                            .queryStringBehavior(OriginRequestQueryStringBehavior.all())
//                            .headerBehavior(
//                                OriginRequestHeaderBehavior.all(
//                                    "CloudFront-Viewer-Country",
//                                    "CloudFront-Viewer-Country-Region",
//                                    "CloudFront-Viewer-City",
//                                    "CloudFront-Viewer-Time-Zone",
//                                    "CloudFront-Viewer-Latitude",
//                                    "CloudFront-Viewer-Longitude",
//                                    "CloudFront-Is-Android-Viewer",
//                                    "CloudFront-Is-IOS-Viewer",
//                                    "CloudFront-Is-Desktop-Viewer",
//                                )
//                            )
//                            .build()
//                    )
                    .allowedMethods(AllowedMethods.ALLOW_ALL)
                    .build()
            )
            .enableLogging(true)
            .logBucket(cfBucket)
            .logFilePrefix("AWSLogs")
            .webAclId(webAclArn.value.toString())
            .errorResponses(
                listOf(
                    ErrorResponse.builder()
                        .httpStatus(500)
                        .ttl(Duration.seconds(3))
                        .build(),
                    ErrorResponse.builder()
                        .httpStatus(502)
                        .ttl(Duration.seconds(3))
                        .build(),
                    ErrorResponse.builder()
                        .httpStatus(503)
                        .ttl(Duration.seconds(3))
                        .build(),
                    ErrorResponse.builder()
                        .httpStatus(400)
                        .ttl(Duration.seconds(60))
                        .build(),
                    ErrorResponse.builder()
                        .httpStatus(403)
                        .ttl(Duration.seconds(60))
                        .build(),
                )
            )
            .priceClass(PriceClass.PRICE_CLASS_200)
            .build()

        return cloudFront
    }

    private fun createRestApi(
        prdLogGroup: LogGroup,
        apiKeyValue: String
    ): RestApi {
        val restApi = RestApi.Builder.create(this, "RestApi")
            .description("Rest API")
            .deploy(true)
            .endpointConfiguration(
                EndpointConfiguration.builder()
                    .types(listOf(EndpointType.REGIONAL))
                    .build()
            )
            .deployOptions(
                StageOptions.builder()
                    .accessLogDestination(LogGroupLogDestination(prdLogGroup))
                    .accessLogFormat(AccessLogFormat.jsonWithStandardFields())
                    .throttlingRateLimit(1)
                    .throttlingBurstLimit(1)
                    .loggingLevel(MethodLoggingLevel.INFO)
                    .tracingEnabled(true)
                    .metricsEnabled(true)
                    .build()
            )
            .build()

        val plan = restApi.addUsagePlan(
            "CFUsagePlan", UsagePlanProps.builder()
                .description("CloudFront usage only")
                .apiStages(
                    listOf(
                        UsagePlanPerApiStage.builder()
                            .api(restApi)
                            .stage(restApi.deploymentStage)
                            .build()
                    )
                )
                .throttle(
                    ThrottleSettings.builder()
                        .rateLimit(1)
                        .burstLimit(1)
                        .build()
                )
                .build()
        )

        val apiKey = restApi.addApiKey(
            "ApiKey", ApiKeyOptions.builder()
                .value(apiKeyValue)
                .build()
        )

        plan.addApiKey(apiKey)

        Alarm.Builder.create(this, "AlarmApiServerErrors")
            .metric(restApi.metricServerError())
            .threshold(1)
            .evaluationPeriods(1)
            .alarmDescription("Alarm if a Server error occurs")
            .build()

        CfnOutput(
            this, "RestApiUrlOutput", CfnOutputProps.builder()
                .value(restApi.url)
                .build()
        )


        return restApi
    }

    private fun createGetDataLambda(
        props: ApiStackProps,
        optimizationLayer: LayerVersion
    ): Alias {
        val lambda = Function.Builder.create(this, "LambdaFunction")
            .description("Test API function")
            .code(Code.fromAsset(props.lambdaArtifacts.getProperty("com.nicolasmilliard.testcdkpipeline_get-data-lambda")))
            .handler("com.nicolasmilliard.testcdkpipeline.GetData")
            .runtime(Runtime.JAVA_11)
            .timeout(Duration.seconds(30))
            .memorySize(512)
            .reservedConcurrentExecutions(1)
            .environment(
                mapOf(
                    "DDB_TABLE_NAME" to props.table.tableName,
                    "POWERTOOLS_SERVICE_NAME" to "api_service",
                    "POWERTOOLS_LOG_LEVEL" to "DEBUG",
                    "POWERTOOLS_METRICS_NAMESPACE" to "api_space",
                    "POWERTOOLS_TRACER_CAPTURE_RESPONSE" to "false",
                    "POWERTOOLS_TRACER_CAPTURE_ERROR" to "true",
                    "AWS_LAMBDA_EXEC_WRAPPER" to "/opt/layer/java-exec-wrapper"
                )
            )
            .layers(listOf(optimizationLayer))
            .logRetention(RetentionDays.ONE_MONTH)
            .tracing(Tracing.ACTIVE)
            .insightsVersion(LambdaInsightsVersion.VERSION_1_0_98_0)
            .build()

        lambda.role!!.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("CloudWatchLambdaInsightsExecutionRolePolicy"))
        lambda.role!!.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("AWSXRayDaemonWriteAccess"))

        props.table.grantReadData(lambda)

        val alias = Alias.Builder.create(this, "HandlerAlias")
            .aliasName("Current")
            .version(lambda.currentVersion)
            .build()
        return alias
    }

    private fun createTieredCompilationLambdaLayer(props: ApiStackProps): LayerVersion {
        val optimizationLayer = LayerVersion.Builder.create(this, "TieredCompilationLayer")
            .layerVersionName("TieredCompilationLayer")
            .description("Enable tiered compilation")
            .compatibleRuntimes(listOf(Runtime.JAVA_11, Runtime.JAVA_8_CORRETTO))
            .code(Code.fromAsset(props.lambdaArtifacts.getProperty("com.nicolasmilliard.testcdkpipeline_lambda-tiered-compilation-layer")))
            .build()
        return optimizationLayer
    }

    private fun createCloudFrontLoggingBucket(): Bucket {
        val bucket = Bucket.Builder.create(this, "CfLogBucket")
            .encryption(BucketEncryption.S3_MANAGED)
            .versioned(false)
            .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
            .removalPolicy(RemovalPolicy.RETAIN)
            .build()

        bucket.addToResourcePolicy(
            PolicyStatement.Builder.create()
                .sid("HttpsOnly")
                .resources(listOf("${bucket.bucketArn}/*", bucket.bucketArn))
                .actions(listOf("*"))
                .principals(listOf(AnyPrincipal()))
                .effect(Effect.DENY)
                .conditions(mapOf("Bool" to mapOf("aws:SecureTransport" to "false")))
                .build()
        )

        val loggingBucketResource = bucket.node.findChild("Resource") as CfnBucket
        // Override accessControl configuration and add metadata for the logging bucket
        loggingBucketResource.addPropertyOverride("AccessControl", "LogDeliveryWrite")
        // Remove the default LifecycleConfiguration for the Logging Bucket
        loggingBucketResource.addPropertyDeletionOverride("LifecycleConfiguration.Rules")

        return bucket
    }
}


interface ApiStackProps : StackProps {
    val table: Table
    val lambdaArtifacts: Properties
}