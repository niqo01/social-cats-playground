package com.nicolasmilliard.socialcatsaws.backend.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import org.slf4j.LoggerFactory

private val  logger = LoggerFactory.getLogger(GetItemHandler::class.java)

class GetItemHandler : RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    override fun handleRequest(
        input: APIGatewayV2HTTPEvent,
        context: Context
    ): APIGatewayV2HTTPResponse {
        logger.debug("Input: $input")

        val headers = mapOf("Content-Type" to "application/json")
        val response = APIGatewayV2HTTPResponse.builder()
            .withStatusCode(200)
            .withHeaders(headers)
            .withBody("""
                {
                 "Hello": "World"
                }
                """)
            .withIsBase64Encoded(false)
            .build()
        logger.debug("Response: $response")
        return response
    }
}