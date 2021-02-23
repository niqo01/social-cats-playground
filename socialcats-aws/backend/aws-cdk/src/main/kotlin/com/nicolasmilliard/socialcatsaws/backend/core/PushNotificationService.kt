package com.nicolasmilliard.socialcatsaws.backend.core

import com.nicolasmilliard.socialcatsaws.backend.util.LambdaFunction
import com.nicolasmilliard.socialcatsaws.backend.util.buildLambdaProps
import com.nicolasmilliard.socialcatsaws.backend.util.getLambdaInsightPolicy
import software.amazon.awscdk.core.CfnOutput
import software.amazon.awscdk.core.CfnOutputProps
import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awsconstructs.services.lambdadynamodb.LambdaToDynamoDB
import java.util.Properties

class PushNotificationService(
  scope: Construct,
  id: String,
  functionsProp: Properties,
  isProd: Boolean,
  appName: String,
  region: String,
  table: Table,
) :
  Construct(scope, id) {

  val createDeviceFunction: LambdaFunction

  init {

    val deviceToDynamoDB = LambdaToDynamoDB.Builder.create(this, "DeviceLambda")
      .existingTableObj(table)
      .lambdaFunctionProps(
        buildLambdaProps(
          construct = this,
          asset = functionsProp.getProperty("create-device-dynamo"),
          region = region,
          handler = "com.nicolasmilliard.socialcatsaws.pushnotifications.backend.functions.OnNewDevice",
          description = "Function for creating Device token to dynamo",
          version = "1.0.0-SNAPSHOT",
          layerId = "CreateDeviceLayerId",
          env = mapOf(
            "APP_NAME" to appName
          )
        )
      )
      .tablePermissions("Write")
      .build()
    createDeviceFunction = deviceToDynamoDB.lambdaFunction
    createDeviceFunction.addToRolePolicy(getLambdaInsightPolicy())

    CfnOutput(
      this,
      "DeviceLambdaOutput",
      CfnOutputProps.builder()
        .value("${createDeviceFunction.functionName}-${createDeviceFunction.currentVersion.version}")
        .description("Function use to store device token")
        .build()
    )
  }
}
