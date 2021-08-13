package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.CfnOutputProps
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.dynamodb.*
import software.constructs.Construct

class DbStack(scope: Construct, id: String, props: StackProps? = null) : Stack(scope, id, props) {

    val tableName: CfnOutput

    init {
        val table = Table.Builder.create(
            this, "TestTable"
        )
            .billingMode(BillingMode.PAY_PER_REQUEST)
            .partitionKey(Attribute.builder().name("pk").type(AttributeType.STRING).build())
            .build()

        tableName = CfnOutput(
            this, "TableNameOutput", CfnOutputProps.builder()
                .value(table.tableName)
                .build()
        )
    }
}