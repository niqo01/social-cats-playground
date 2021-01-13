package com.nicolasmilliard.socialcatsaws.imageprocessing.backend.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.nicolasmilliard.cloudmetric.CloudMetricModule
import com.nicolasmilliard.cloudmetric.Unit
import com.nicolasmilliard.socialcatsaws.profile.ImageUseCaseModule
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class OnNewImage : RequestHandler<S3Event, S3Event> {

  private val cloudMetrics = CloudMetricModule.provideCloudMetrics(System.getenv("APP_NAME"))

  private val useCase = ImageUseCaseModule.provideUploadImageUseCase(
    System.getenv("DDB_TABLE_NAME"),
    System.getenv("S3_BUCKET_NAME"),
    cloudMetrics,
    System.getenv("AWS_REGION")
  )

  override fun handleRequest(
    event: S3Event,
    context: Context
  ): S3Event {
    cloudMetrics.putProperty("RequestId", context.awsRequestId)
    logger.info("event=image_store_new_image")
    cloudMetrics.putMetric("NewStoredImageCount", 1.0, Unit.COUNT)
    val s3Object = event.records[0].s3.`object`
    useCase.onNewStoredImage(s3Object.key, s3Object.sizeAsLong)
    cloudMetrics.flush()
    return event
  }
}
