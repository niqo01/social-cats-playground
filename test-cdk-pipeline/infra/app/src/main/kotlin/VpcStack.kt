package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.*
import software.amazon.awscdk.Stack
import software.amazon.awscdk.cloudformation.include.CfnInclude
import software.amazon.awscdk.services.apigateway.*
import software.amazon.awscdk.services.cloudfront.*
import software.amazon.awscdk.services.cloudfront.origins.HttpOrigin
import software.amazon.awscdk.services.cloudwatch.Alarm
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.ec2.*
import software.amazon.awscdk.services.ec2.SecurityGroup.*
import software.amazon.awscdk.services.iam.AnyPrincipal
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.ManagedPolicy
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.lambda.*
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.logs.LogGroup
import software.amazon.awscdk.services.logs.RetentionDays
import software.amazon.awscdk.services.rds.SubnetGroup
import software.amazon.awscdk.services.s3.BlockPublicAccess
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.s3.BucketEncryption
import software.amazon.awscdk.services.s3.CfnBucket
import software.constructs.Construct
import java.util.*

class VpcStack(scope: Construct, id: String, props: StackProps) :
    Stack(scope, id, props) {

    val mainVpc: MainVpc

    init {
        val privateIsolatedSubnetName = "DbSubnetGroup"
        val privateEgressSubnetName = "PrivEgSn"

        val vpc = Vpc.Builder.create(scope, "MainVPC")
            .vpcName("MainVpc")
            .ipAddresses(IpAddresses.cidr("10.0.0.0/16"))
            .enableDnsSupport(true)
            .enableDnsHostnames(true)
            .defaultInstanceTenancy(DefaultInstanceTenancy.DEFAULT)
            .maxAzs(3)
            .subnetConfiguration(
                listOf(
                    SubnetConfiguration.builder()
                        .cidrMask(17)
                        .name(privateIsolatedSubnetName)
                        .subnetType(SubnetType.PRIVATE_ISOLATED)
                        .build(),
                    SubnetConfiguration.builder()
                        .cidrMask(24)
                        .name(privateEgressSubnetName)
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS).build()
                )
            )
            .build()

        mainVpc = MainVpc(
            vpc = vpc,
            privateIsolatedSubnetName = privateIsolatedSubnetName,
            privateEgressSubnetName = privateEgressSubnetName
        )
    }
}

data class MainVpc(
    val vpc: Vpc,
    val privateIsolatedSubnetName: String,
    val privateEgressSubnetName: String,
)