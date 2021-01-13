package com.nicolasmilliard.cloudmetric

import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger
import software.amazon.cloudwatchlogs.emf.model.DimensionSet

private typealias CloudWatchUnit = software.amazon.cloudwatchlogs.emf.model.Unit

internal class CloudWatchMetrics(namespace: String) : CloudMetrics {
  private val logger = MetricsLogger()

  override fun putDimension(d1: String, v1: String) {
    logger.putDimensions(DimensionSet.of(d1, v1))
  }

  override fun putProperty(key: String, payload: Map<String, Any>) {
    logger.putProperty(key, payload)
  }

  override fun putProperty(key: String, value: Any) {
    logger.putProperty(key, value)
  }

  override fun putMetric(key: String, value: Double, unit: Unit) {
    logger.putMetric(key, value, unit.toWatchUnit())
  }

  override fun flush() {
    logger.flush()
  }

  private fun Unit.toWatchUnit() =
    when (this) {
      Unit.SECONDS -> CloudWatchUnit.SECONDS
      Unit.MICROSECONDS -> CloudWatchUnit.MICROSECONDS
      Unit.MILLISECONDS -> CloudWatchUnit.MILLISECONDS
      Unit.BYTES -> CloudWatchUnit.BYTES
      Unit.KILOBYTES -> CloudWatchUnit.KILOBYTES
      Unit.MEGABYTES -> CloudWatchUnit.MEGABYTES
      Unit.GIGABYTES -> CloudWatchUnit.GIGABYTES
      Unit.TERABYTES -> CloudWatchUnit.TERABYTES
      Unit.BITS -> CloudWatchUnit.BITS
      Unit.KILOBITS -> CloudWatchUnit.KILOBITS
      Unit.MEGABITS -> CloudWatchUnit.MEGABITS
      Unit.GIGABITS -> CloudWatchUnit.GIGABITS
      Unit.TERABITS -> CloudWatchUnit.TERABITS
      Unit.PERCENT -> CloudWatchUnit.PERCENT
      Unit.COUNT -> CloudWatchUnit.COUNT
      Unit.BYTES_SECOND -> CloudWatchUnit.BYTES_SECOND
      Unit.KILOBYTES_SECOND -> CloudWatchUnit.KILOBYTES_SECOND
      Unit.MEGABYTES_SECOND -> CloudWatchUnit.MEGABITS_SECOND
      Unit.GIGABYTES_SECOND -> CloudWatchUnit.GIGABYTES_SECOND
      Unit.TERABYTES_SECOND -> CloudWatchUnit.TERABYTES_SECOND
      Unit.BITS_SECOND -> CloudWatchUnit.BITS_SECOND
      Unit.KILOBITS_SECOND -> CloudWatchUnit.KILOBITS_SECOND
      Unit.MEGABITS_SECOND -> CloudWatchUnit.MEGABITS_SECOND
      Unit.GIGABITS_SECOND -> CloudWatchUnit.GIGABITS_SECOND
      Unit.TERABITS_SECOND -> CloudWatchUnit.TERABITS_SECOND
      Unit.COUNT_SECOND -> CloudWatchUnit.COUNT_SECOND
      Unit.NONE -> CloudWatchUnit.NONE
    }
}
