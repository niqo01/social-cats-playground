package com.nicolasmilliard.socialcats.searchapi

import com.google.common.truth.Truth.assertThat
import com.nicolasmilliard.socialcats.model.SearchUsersResult
import com.nicolasmilliard.socialcats.model.User
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.Test

class ApplicationTest {

    private fun MapApplicationConfig.setConfig() {
        put("env.isProduction", "false")
        put("google.projectId", "projectId")
        put("elasticSearch.useAws", "false")
        put("elasticSearch.endpoint", "http://endpoint")
        put("elasticSearch.apiKeyId", "apiKeyId")
        put("elasticSearch.apiKey", "apiKey")
    }

    @Test
    fun testRoot() {
        withTestApplication({
            (environment.config as MapApplicationConfig).setConfig()
            module(testing = true)
        }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo("Hello World")
            }
        }
    }

    @Test
    fun testSearchUnAuthenticated() {
        withTestApplication({
            (environment.config as MapApplicationConfig).setConfig()
            module(testing = true)
        }) {
            handleRequest(HttpMethod.Get, "/v1/search").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }

    val aSearchUserResult = SearchUsersResult(
        3,
        listOf(User("id", "name", null), User("id2", "name2", "photoUrl2"), User("id3", "415 123 1234", "photoUrl3"))
    )

    @Test
    fun testSearchAuthenticated() {
        withTestApplication({
            (environment.config as MapApplicationConfig).setConfig()
            module(testing = true)
        }) {
            handleRequest(HttpMethod.Get, "/v1/search") {
                addHeader("Authorization", "Bearer $TEST_VALID_TOKEN")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                val json = Json(JsonConfiguration.Default)
                assertThat(response.content).isNotEmpty()
                val result = json.parse(SearchUsersResult.serializer(), response.content!!)
                assertThat(result).isEqualTo(aSearchUserResult)
            }
        }
    }

    @Test
    fun testSearchInvalidAuthenticated() {
        withTestApplication({
            (environment.config as MapApplicationConfig).setConfig()
            module(testing = true)
        }) {
            handleRequest(HttpMethod.Get, "/v1/search") {
                addHeader("Authorization", "Bearer invalid")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }
}
