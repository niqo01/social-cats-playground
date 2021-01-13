package com.nicolasmilliard.socialcatsaws.backend

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.StackProps
import java.io.IOException
import java.util.Properties

class ApiStackTest {
  @Test
  @Throws(IOException::class)
  fun testApiStack() {
    val app = App()
    val service = SocialCatsService(
      app,
      "test",
      StackProps.builder().build(),
      Properties(),
      false,
      "Test"
    )

    // synthesize the stack to a CloudFormation template and compare against
    // a checked-in JSON file.
    val actual =
      JSON.valueToTree<JsonNode>(app.synth().getStackArtifact(service.artifactId).template)
    Assertions.assertThat(ObjectMapper().createObjectNode()).isEqualTo(actual)
  }

  companion object {
    private val JSON = ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true)
  }
}
