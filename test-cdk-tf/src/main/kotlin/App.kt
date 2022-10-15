package com.nicolasmilliard.testcdktfpipeline

import com.hashicorp.cdktf.App

fun main() {
    val app = App()
    MainStack(app, "gcp-cloud-run")
    app.synth()
}

