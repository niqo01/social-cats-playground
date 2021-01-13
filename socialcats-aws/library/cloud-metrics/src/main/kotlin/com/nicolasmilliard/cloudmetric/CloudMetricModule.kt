package com.nicolasmilliard.cloudmetric

public object CloudMetricModule {

  public fun provideCloudMetrics(appName: String): CloudMetrics = CloudWatchMetrics(appName)
}
