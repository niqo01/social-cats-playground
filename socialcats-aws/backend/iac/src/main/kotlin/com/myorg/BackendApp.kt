package com.myorg

import kotlin.jvm.JvmStatic
import software.amazon.awscdk.core.App
import java.io.File
import java.io.FileInputStream
import java.util.*

object BackendApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val app = App()

        val functionsProp = Properties()
        FileInputStream(File(args[0])).use { functionsProp.load(it) }

        BackendStack(app, "BackendStack", functionsProp = functionsProp)
        app.synth()
    }
}