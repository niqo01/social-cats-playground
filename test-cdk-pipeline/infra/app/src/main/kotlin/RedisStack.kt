package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.*
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.Port
import software.amazon.awscdk.services.ec2.SecurityGroup
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.elasticache.CfnReplicationGroup
import software.amazon.awscdk.services.elasticache.CfnSubnetGroup
import software.constructs.Construct

class RedisStack(scope: Construct, id: String, props: GraphQlStackProps) :
    Stack(scope, id, props) {

    private val redisPort = 6379

    private val redisSg: SecurityGroup
    private val redisCluster : CfnReplicationGroup;

    fun addIngressRule(peer: software.amazon.awscdk.services.ec2.IPeer) {
        // Redis SG accepts TCP connections from the Lambda SG on Redis port.
        redisSg.addIngressRule(peer, Port.tcp(redisPort))
    }

    init {

        // Create two different security groups:
        // One for the redis cluster, one for the lambda function.
        // This is to allow traffic only from our functions to the redis cluster
        redisSg = SecurityGroup.Builder.create(scope, "redisSg")
            .vpc(props.mainVpc.vpc)
            .description("Security group for Redis Cluster")
            .build()

        val redisSubnets = CfnSubnetGroup.Builder.create(scope, "RedisSubnets")
            .cacheSubnetGroupName("RedisSubnets")
            .description("Subnet Group for Redis Cluster")
            .subnetIds(
                props.mainVpc.vpc.selectSubnets(
                    SubnetSelection.builder().subnetGroupName(props.mainVpc.privateIsolatedSubnetName).build()
                ).subnetIds
            )
            .build()

        redisCluster = CfnReplicationGroup.Builder.create(scope, "PresenceCluster")
            .replicationGroupDescription("PresenceReplicationGroup")
            .cacheNodeType("cache.t3.small")
            .engine("redis")
            .numCacheClusters(2)
            .automaticFailoverEnabled(true)
            .multiAzEnabled(true)
            .cacheSubnetGroupName(redisSubnets.ref)
            .securityGroupIds(listOf(redisSg.securityGroupId))
            .port(redisPort)
            .build()
    }

}


interface RedisStackProps : StackProps {
    val mainVpc: MainVpc
}