package com.nicolasmilliard

import com.nicolasmilliard.socialcats.model.SearchUsersResult
import com.nicolasmilliard.socialcats.search.repository.aSearchResultNoInput
import com.nicolasmilliard.socialcats.searchapi.TEST_VALID_TOKEN
import com.nicolasmilliard.socialcats.searchapi.module
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.Test

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello World", response.content)
            }
        }
    }

    @Test
    fun testSearchUnAuthenticated() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/v1/search").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val json = Json(JsonConfiguration.Default)
                assertNotNull(response.content)
                val result = json.parse(SearchUsersResult.serializer(), response.content!!)
                assertEquals(aSearchResultNoInput, result)
            }
        }
    }

    @Test
    fun testSearchAuthenticated() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/v1/search") {
                addHeader("Authorization", "Bearer $TEST_VALID_TOKEN")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val json = Json(JsonConfiguration.Default)
                assertNotNull(response.content)
                val result = json.parse(SearchUsersResult.serializer(), response.content!!)
                assertEquals(aSearchResultNoInput, result)
            }
        }
    }

    @Test
    fun testSearchInvalidAuthenticated() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/v1/search") {
                addHeader("Authorization", "Bearer invalid")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }
}
