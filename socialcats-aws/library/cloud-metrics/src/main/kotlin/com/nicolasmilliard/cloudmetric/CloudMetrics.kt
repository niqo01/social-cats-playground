package com.nicolasmilliard.cloudmetric

public interface CloudMetrics {
  public fun putDimension(d1: String, v1: String)
  public fun putProperty(key: String, value: Any)
  public fun putProperty(key: String, payload: Map<String, Any>)
  public fun putMetric(key: String, value: Double, unit: Unit)
  public fun flush()
}

public enum class Unit {
  SECONDS,
  MICROSECONDS,
  MILLISECONDS,
  BYTES,
  KILOBYTES,
  MEGABYTES,
  GIGABYTES,
  TERABYTES,
  BITS,
  KILOBITS,
  MEGABITS,
  GIGABITS,
  TERABITS,
  PERCENT,
  COUNT,
  BYTES_SECOND,
  KILOBYTES_SECOND,
  MEGABYTES_SECOND,
  GIGABYTES_SECOND,
  TERABYTES_SECOND,
  BITS_SECOND,
  KILOBITS_SECOND,
  MEGABITS_SECOND,
  GIGABITS_SECOND,
  TERABITS_SECOND,
  COUNT_SECOND,
  NONE,
}
