package com.nicolasmilliard.socialcatsaws.backend.core

import software.amazon.awscdk.core.CfnOutput
import software.amazon.awscdk.core.CfnOutputProps
import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.RemovalPolicy
import software.amazon.awscdk.services.ec2.GatewayVpcEndpointAwsService
import software.amazon.awscdk.services.ec2.GatewayVpcEndpointOptions
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.Port
import software.amazon.awscdk.services.ec2.SecurityGroup
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.ec2.VpcLookupOptions
import software.amazon.awscdk.services.glue.CfnClassifier
import software.amazon.awscdk.services.glue.CfnCrawler
import software.amazon.awscdk.services.glue.Connection
import software.amazon.awscdk.services.glue.ConnectionType
import software.amazon.awscdk.services.glue.Database
import software.amazon.awscdk.services.iam.AnyPrincipal
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.PolicyDocument
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.iam.Role
import software.amazon.awscdk.services.iam.ServicePrincipal
import software.amazon.awscdk.services.s3.BlockPublicAccess
import software.amazon.awscdk.services.s3.Bucket

class S3Monitoring constructor(
  scope: Construct,
  id: String,
  isProd: Boolean,
  accesslogBuckets: List<Bucket>
) :
  Construct(scope, id) {

  init {

    val defaultVpc = createVpcEndpointForS3()

    val database = Database.Builder.create(this, "S3LogDatabase")
      .databaseName("s3-logs-database")
      .build()
    setupS3GlueCrawler(defaultVpc, database, accesslogBuckets)
    createAthenaQueryResultBucket(isProd)
  }

  private fun setupS3GlueCrawler(defaultVpc: IVpc, database: Database, accesslogBuckets: List<Bucket>) {
    val securityGroup = SecurityGroup.Builder.create(this, "SecurityGroup")
      .allowAllOutbound(true)
      .vpc(defaultVpc)
      .build()

    securityGroup.addIngressRule(
      securityGroup,
      Port.allTraffic(),
      "Allowing all ingress ports."
    )

    Connection.Builder.create(this, "GlueS3Connection")
      .connectionName("GlueS3Connection")
      .description("S3 connection.")
      .type(ConnectionType.NETWORK)
      .securityGroups(listOf(securityGroup))
      .subnet(defaultVpc.selectSubnets().subnets[0])
      .build()

    CfnClassifier.Builder.create(this, "S3ServerLogsClassifier")
      .grokClassifier(
        CfnClassifier.GrokClassifierProperty.builder()
          .name("S3ServerAccessLogClassifier")
          .classification("s3_server_access_logs")
          .grokPattern("%{UUID:bucket_owner} %{WORD:bucket} %{DATESTAMP_EU:time} %{IP:remote_ip} %{UUID:requester} %{UUID:request_id} %{WORD:operation} (%{PATH}|-) %{QUOTEDSTRING:request_uri} %{NUMBER:http_status} %{GREEDYDATA}")
          .build()
      )
      .build()
    val crawlerRole = Role.Builder.create(this, "CrawlerRole")
      .assumedBy(
        ServicePrincipal.Builder.create("glue.amazonaws.com").build()
      )
      .path("/")
      .inlinePolicies(
        mapOf(
          "root" to PolicyDocument.Builder.create().statements(
            listOf(
              PolicyStatement.Builder.create().effect(Effect.ALLOW)
                .actions(listOf("*"))
                .resources(listOf("*"))
                .build()
            )
          ).build()
        )
      )
      .build()
    CfnCrawler.Builder.create(this, "S3Crawler")
      .description("Crawler of s3 server access log")
      .databaseName(database.databaseName)
      .role(crawlerRole.roleArn)
      .targets(
        CfnCrawler.TargetsProperty.builder()
          .s3Targets(
            accesslogBuckets.map {
              CfnCrawler.S3TargetProperty.builder()
                .path(it.s3UrlForObject())
                .connectionName("S3Connection")
                .build()
            }
          )
          .build()
      )
      .classifiers(listOf("S3ServerAccessLogClassifier"))
      .build()
  }

  private fun createVpcEndpointForS3(): IVpc {
    val vpc =
      Vpc.fromLookup(this, "DefaultVPC", VpcLookupOptions.builder().isDefault(true).build())
    val s3Endpoint = vpc.addGatewayEndpoint(
      "S3GatewayEndpoint",
      GatewayVpcEndpointOptions.builder()
        .service(GatewayVpcEndpointAwsService.S3)
        .build()
    )
    s3Endpoint.addToPolicy(
      PolicyStatement.Builder.create()
        .principals(listOf(AnyPrincipal()))
        .actions(listOf("*"))
        .resources(listOf("*"))
        .effect(Effect.ALLOW)
        .build()
    )
    return vpc
  }

  private fun createAccessLogBucket(isProd: Boolean): Bucket {
    val removalPolicy = if (isProd) RemovalPolicy.RETAIN else RemovalPolicy.DESTROY

    val bucket = Bucket.Builder.create(this, "AccessLogsBucket")
      .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
      .versioned(false)
      .removalPolicy(removalPolicy)
      .autoDeleteObjects(!isProd)
      .build()

    CfnOutput(
      this,
      "AccessLogsBucketOutput",
      CfnOutputProps.builder()
        .value(bucket.bucketArn)
        .description("S3 Bucket use to store access logs")
        .build()
    )
    return bucket
  }

  private fun createAthenaQueryResultBucket(isProd: Boolean): Bucket {
    val removalPolicy = if (isProd) RemovalPolicy.RETAIN else RemovalPolicy.DESTROY

    val bucket = Bucket.Builder.create(this, "AthenaQueryResultBucket")
      .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
      .versioned(false)
      .removalPolicy(removalPolicy)
      .autoDeleteObjects(!isProd)
      .build()

    CfnOutput(
      this,
      "AthenaQueryResultBucketOutput",
      CfnOutputProps.builder()
        .value(bucket.bucketArn)
        .description("S3 Bucket use to store Athena query Result")
        .build()
    )
    return bucket
  }
}
