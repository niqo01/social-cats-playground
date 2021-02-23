package com.nicolasmilliard.socialcatsaws.imageprocessing.backend.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class OnNewImage(appComponent: AppComponent = DaggerAppComponent.create()) : RequestHandler<S3Event, S3Event> {

  private val cloudMetrics = appComponent.getCloudMetrics()
  private val useCase = appComponent.getUploadImageUseCase()

  override fun handleRequest(
    event: S3Event,
    context: Context
  ): S3Event {
    cloudMetrics.putProperty("RequestId", context.awsRequestId)
    logger.info("event=image_store_new_image")
    event.records.forEach {
      val s3Object = it.s3.`object`
      useCase.onNewStoredImage(s3Object.key, s3Object.sizeAsLong, it.eventTime.toString())
    }

    cloudMetrics.flush()
    return event
  }
}
