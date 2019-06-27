package com.nicolasmilliard.socialcats.data

import java.nio.charset.StandardCharsets
import java.util.Base64
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext

class ElasticServiceInterceptor(id: String, apiKey: String) : HttpRequestInterceptor {

    val authHeader: String = Base64.getEncoder().encodeToString("$id:$apiKey".toByteArray(StandardCharsets.UTF_8))

    override fun process(request: HttpRequest?, context: HttpContext?) {
        request?.addHeader("Authorization", "ApiKey $authHeader")
    }
}
