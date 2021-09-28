package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.*
import software.amazon.awscdk.services.cloudwatch.Alarm
import software.amazon.awscdk.services.dynamodb.Attribute
import software.amazon.awscdk.services.dynamodb.AttributeType
import software.amazon.awscdk.services.dynamodb.BillingMode
import software.amazon.awscdk.services.dynamodb.Table
import software.constructs.Construct

class DbStack(scope: Construct, id: String, props: DbStackProps) :
    Stack(scope, id, props) {

    val table: Table

    init {
        table = Table.Builder.create(this, "TestTable")
            .billingMode(BillingMode.PAY_PER_REQUEST)
            .partitionKey(
                Attribute.builder()
                    .name("pk")
                    .type(AttributeType.STRING)
                    .build()
            )
            .pointInTimeRecovery(true)
            .removalPolicy(props.removalPolicy)
            .build()

        Tags.of(table).add("name", "test-table")

        Alarm.Builder.create(this, "AlarmDynamoUserErrors")
            .metric(table.metricUserErrors())
            .threshold(1)
            .evaluationPeriods(1)
            .alarmDescription("Alarm if a Dynamo user errors occur")
            .build()

        CfnOutput(
            this, "TableNameOutput", CfnOutputProps.builder()
                .value(table.tableName)
                .build()
        )
    }
}

interface DbStackProps : StackProps {
    val removalPolicy: RemovalPolicy
}