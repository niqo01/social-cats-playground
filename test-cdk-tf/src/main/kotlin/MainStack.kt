package com.nicolasmilliard.testcdktfpipeline

import software.constructs.Construct

import com.hashicorp.cdktf.TerraformStack
import com.hashicorp.cdktf.providers.google.cloud_run_service.CloudRunService
import com.hashicorp.cdktf.providers.google.cloud_run_service.CloudRunServiceConfig
import com.hashicorp.cdktf.providers.google.provider.GoogleProvider
import com.hashicorp.cdktf.providers.google.provider.GoogleProviderConfig

class MainStack(scope: Construct, id: String): TerraformStack(scope, id) {
    init {
        val local = "asia-east1"
        GoogleProvider(this, "GoogleRun", GoogleProviderConfig.builder()
            .region(local)
            .zone("$local-c")
            .project("testterraform2")
            .credentials("")
            .build())

        CloudRunService(this, "GcpCDKCloudrunsvc", CloudRunServiceConfig.builder()
            .location(local)
            .name("cloudrun")
            .build())
    }
}