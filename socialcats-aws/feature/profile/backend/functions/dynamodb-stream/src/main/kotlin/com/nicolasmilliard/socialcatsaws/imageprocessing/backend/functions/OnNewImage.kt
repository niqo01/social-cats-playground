package com.nicolasmilliard.socialcatsaws.imageprocessing.backend.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class OnNewImage(appComponent: AppComponent = DaggerAppComponent.create()) : RequestHandler<DynamodbEvent, Unit> {

  private val cloudMetrics = appComponent.getCloudMetrics()

  override fun handleRequest(
    event: DynamodbEvent,
    context: Context
  ) {
    cloudMetrics.putProperty("RequestId", context.awsRequestId)

    event.records.forEach {
      logger.info { "Event received $it" }
    }

    cloudMetrics.flush()
  }
}
