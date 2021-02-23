package com.nicolasmilliard.cloudmetric

public class FakeCloudMetrics : CloudMetrics {

  public val dimensions: MutableMap<String, Any> = mutableMapOf()
  public val metrics: MutableMap<String, Any> = mutableMapOf()
  public val properties: MutableMap<String, Any> = mutableMapOf()

  override fun putDimension(d1: String, v1: String) {
    dimensions[d1] = v1
  }

  override fun putProperty(key: String, value: Any) {
    properties[key] = value
  }

  override fun putProperty(key: String, payload: Map<String, Any>) {
    properties[key] = payload
  }

  override fun putMetric(key: String, value: Double, unit: Unit) {
    metrics[key] = value
  }

  override fun flush() {
    metrics.clear()
  }
}
