package com.nicolasmilliard.socialcatsaws.backend.core

import software.amazon.awscdk.core.CfnOutput
import software.amazon.awscdk.core.CfnOutputProps
import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.RemovalPolicy
import software.amazon.awscdk.services.dynamodb.Attribute
import software.amazon.awscdk.services.dynamodb.AttributeType
import software.amazon.awscdk.services.dynamodb.BillingMode
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.dynamodb.TableEncryption

class UserRepository(
  scope: Construct,
  id: String,
  isProd: Boolean
) : Construct(scope, id) {

  val dynamodbTable: Table

  init {
    dynamodbTable = createDynamoTable(isProd)
  }

  private fun createDynamoTable(isProd: Boolean): Table {

    val removalPolicy = if (isProd) RemovalPolicy.RETAIN else RemovalPolicy.DESTROY

    val table = Table.Builder.create(this, "UserTable")
      .tableName("users")
      .partitionKey(
        Attribute.builder()
          .name("partition_key")
          .type(AttributeType.STRING)
          .build()
      )
      .sortKey(
        Attribute.builder()
          .name("sort_key")
          .type(AttributeType.STRING)
          .build()
      )
      // The default removal policy is RETAIN, which means that cdk destroy will not attempt to delete
      // the new table, and it will remain in your account until manually deleted. By setting the policy to
      // DESTROY, cdk destroy will delete the table (even if it has data in it)
      .removalPolicy(removalPolicy)
      .billingMode(BillingMode.PAY_PER_REQUEST)
      .encryption(TableEncryption.AWS_MANAGED)
      .pointInTimeRecovery(isProd)
      .build()

    CfnOutput(
      this,
      "UserTableOutput",
      CfnOutputProps.builder()
        .value(table.tableName)
        .description("DynamoDB Single table use to store users data")
        .build()
    )
    return table
  }
}
