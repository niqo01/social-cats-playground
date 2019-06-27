package com.nicolasmilliard.socialcats.searchapi

import com.google.common.truth.Truth.assertThat
import com.nicolasmilliard.socialcats.model.SearchUsersResult
import com.nicolasmilliard.socialcats.search.repository.aSearchResultNoInput
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.Test

class ApplicationTest {

    @Test
    fun testRoot() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo("Hello World")
            }
        }
    }

    @Test
    fun testSearchUnAuthenticated() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/v1/search").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                val json = Json(JsonConfiguration.Default)
                assertThat(response.content).isNotEmpty()
                val result = json.parse(SearchUsersResult.serializer(), response.content!!)
                assertThat(result).isEqualTo(aSearchResultNoInput)
            }
        }
    }

    @Test
    fun testSearchAuthenticated() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/v1/search") {
                addHeader("Authorization", "Bearer $TEST_VALID_TOKEN")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                val json = Json(JsonConfiguration.Default)
                assertThat(response.content).isNotEmpty()
                val result = json.parse(SearchUsersResult.serializer(), response.content!!)
                assertThat(result).isEqualTo(aSearchResultNoInput)
            }
        }
    }

    @Test
    fun testSearchInvalidAuthenticated() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/v1/search") {
                addHeader("Authorization", "Bearer invalid")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }
}
