package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.CfnOutputProps
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.dynamodb.BillingMode
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.dynamodb.TableProps
import software.constructs.Construct

class DbStack(scope: Construct, id: String, props: StackProps? = null) : Stack(scope, id, props){
    init {
        val table = Table(
            this, "TestTable", TableProps.builder()
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        )

        CfnOutput(this, "TableNameOutput", CfnOutputProps.builder()
            .value(table.tableName)
            .build())
    }
}