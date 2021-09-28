package com.nicolasmilliard.testcdkpipeline

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import software.amazon.awscdk.App
import software.amazon.awscdk.RemovalPolicy
import java.io.IOException

class DbStackTest {

    private val JSON = ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true)

    @Test
    @Throws(IOException::class)
    fun testStack() {
        val app = App()
        val stack = DbStack(app, "test", object : DbStackProps {
            override val removalPolicy: RemovalPolicy
                get() = RemovalPolicy.DESTROY
        })

        // synthesize the stack to a CloudFormation template and compare against
        // a checked-in JSON file.
        val actual = JSON.valueToTree<JsonNode>(app.synth().getStackArtifact(stack.artifactId).template)
//        assertThat(ObjectMapper().createObjectNode()).isEqualTo(actual)
    }
}