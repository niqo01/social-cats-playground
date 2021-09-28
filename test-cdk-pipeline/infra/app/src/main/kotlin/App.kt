package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.App
import java.io.File
import java.io.FileInputStream
import java.util.*

fun main(args: Array<String>) {
    val app = App()

    val lambdaFile= args[0]

    val lambdaArtifacts = Properties()
    FileInputStream(File(lambdaFile)).use { lambdaArtifacts.load(it) }

//    app.setupStacks("test", false, lambdaArtifacts)
    app.setupPipeline(lambdaArtifacts)

    app.synth()
}

