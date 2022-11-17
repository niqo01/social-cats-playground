package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.services.appsync.alpha.*

object PresenceSchema {

    fun getSchema(): Schema {

        val schema = Schema()

        val requieredId = GraphqlType.id(BaseTypeOptions.builder().isRequired(true).build())

        val status = EnumType.Builder.create("Status").definition(listOf("online", "offline")).build()

        val requiredStatus =
            GraphqlType.intermediate(GraphqlTypeOptions.builder().intermediateType(status).isRequired(true).build())

        val presence = ObjectType.Builder.create("Presence")
            .definition(
                mapOf(
                    "id" to requieredId,
                    "status" to requiredStatus
                )
            )
            .directives(listOf(Directive.oidc(), Directive.apiKey()))
            .build()

        schema.addType(status)
        schema.addType(presence)


        val returnPresence = GraphqlType.intermediate(GraphqlTypeOptions.builder().intermediateType(presence).build())


        // Add queries
        schema.addQuery(
            "heartbeat",
            ResolvableField.Builder.create().returnType(returnPresence).args(mapOf("id" to requieredId)).build()
        )
        schema.addQuery(
            "status",
            ResolvableField.Builder.create().returnType(returnPresence).args(mapOf("id" to requieredId)).build()
        )

        // Add mutations
        schema.addMutation(
            "connect",
            ResolvableField.Builder.create().returnType(returnPresence).args(mapOf("id" to requieredId)).build()
        )
        schema.addMutation(
            "disconnect",
            ResolvableField.Builder.create().returnType(returnPresence).args(mapOf("id" to requieredId)).build()
        )
        schema.addMutation(
            "disconnected",
            ResolvableField.Builder.create().returnType(returnPresence).args(mapOf("id" to requieredId)).directives(
                listOf(Directive.oidc())
            ).build()
        )

        // Add subscription
        schema.addSubscription(
            "onStatus",
            Field.Builder.create()
                .returnType(returnPresence)
                .args(mapOf("id" to requieredId))
                .directives(listOf(Directive.subscribe("connect", "disconnected")))
                .build()
        )
        return schema
    }
}