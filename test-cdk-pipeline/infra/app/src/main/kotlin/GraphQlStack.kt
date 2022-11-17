package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.*
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.appsync.alpha.*
import software.amazon.awscdk.services.ec2.SecurityGroup
import software.constructs.Construct

class GraphQlStack(scope: Construct, id: String, props: GraphQlStackProps) :
    Stack(scope, id, props) {

    init {

        val lambdaSG = SecurityGroup.Builder.create(scope, "lambdaSG")
            .vpc(props.mainVpc.vpc)
            .description("Security group for Lambda functions")
            .build()

        val api = GraphqlApi.Builder.create(this, "PresenceAPI")
            .name("PresenceAPI")
            .authorizationConfig(
                AuthorizationConfig.builder()
                    .defaultAuthorization(
                        AuthorizationMode.builder().openIdConnectConfig(
                            OpenIdConnectConfig.builder()
                                .oidcProvider("https://securetoken.google.com/social-cats-aws")
                                .build()
                        ).build()
                    )
                    .additionalAuthorizationModes(
                        listOf(
                            AuthorizationMode.builder()
                                .apiKeyConfig(
                                    ApiKeyConfig.builder()
                                        .name("GraphQl API key")
                                        .build()
                                )
                                .build()
                        )
                    )
                    .build()
            )
            .logConfig(LogConfig.builder().fieldLogLevel(FieldLogLevel.ALL).build())
            .schema(PresenceSchema.getSchema())
            .build()

        val heartBeatSource = api.addLambdaDataSource("heartbeatDS", )
        api.createResolver(
            ExtendedResolverProps.builder().typeName("Query").fieldName("heartbeat").dataSource(heartBeatSource).build()
        )

        val statusSource = api.addLambdaDataSource("statusDS", )
        api.createResolver(
            ExtendedResolverProps.builder().typeName("Query").fieldName("status").dataSource(statusSource).build()
        )

        api.createResolver(
            ExtendedResolverProps.builder().typeName("Mutation").fieldName("connect").dataSource(heartBeatSource)
                .build()
        )

        val disconnectSource = api.addLambdaDataSource("disconnectDS", )
        api.createResolver(
            ExtendedResolverProps.builder().typeName("Mutation").fieldName("disconnect").dataSource(disconnectSource)
                .build()
        )

        // The "disconnected" mutation is called on disconnection, and
        // is the one AppSync client will subscribe too.
        // It uses a NoneDataSource with simple templates passing its argument,
        // so that it could trigger the notifications.
        val noneDS = api.addNoneDataSource("disconnectedDS")
        val context = "\$context"
        val requestMappingTemplate = MappingTemplate.fromString(
            """
        {
            "version": "2017-02-28",
            "payload": {
            "id": "$context.arguments.id",
            "status": "offline"
        }
        }
        """
        )
        val util = "\$util"
        val responseMappingTemplate = MappingTemplate.fromString(
            """
        $util.toJson($context.result)
        """
        );
        api.createResolver(
            ExtendedResolverProps.builder()
                .typeName("Mutation")
                .fieldName("disconnected")
                .dataSource(noneDS)
                .requestMappingTemplate(requestMappingTemplate)
                .responseMappingTemplate(responseMappingTemplate)
                .build()
        )
    }

}


interface GraphQlStackProps : StackProps {
    val mainVpc: MainVpc
}