package com.nicolasmilliard.testcdkpipeline

import org.junit.jupiter.api.Test
import software.amazon.awscdk.App
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.assertions.Template
import java.io.IOException

class DbStackTest {

    @Test
    @Throws(IOException::class)
    fun testStack() {
        val app = App()
        val stack = DbStack(app, "test", object : DbStackProps {
            override val removalPolicy: RemovalPolicy
                get() = RemovalPolicy.DESTROY
        })

        // Uncommenting this one result in CDK ls to show only test stack????
//        val template = Template.fromStack(stack)
//        template.resourceCountIs("AWS::DynamoDB::Table", 1)
    }
}