package com.nicolasmilliard.socialcats.searchapi

import com.google.common.truth.Truth.assertThat
import com.nicolasmilliard.socialcats.model.SearchUsersResult
import com.nicolasmilliard.socialcats.model.User
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import org.koin.test.AutoCloseKoinTest

class SearchTest : AutoCloseKoinTest() {

    @Test
    fun testSearchUnAuthenticated() {
        val fakes = TestAppComponent()
        withTestApplication({
            (environment.config as MapApplicationConfig).setConfig()
            module(fakes.getTestModules(environment.config))
        }) {
            with(handleRequest(HttpMethod.Get, "/v1/search")) {
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
        val fakes = TestAppComponent()
        withTestApplication({
            (environment.config as MapApplicationConfig).setConfig()
            module(fakes.getTestModules(environment.config))
        }) {
            with(
                handleRequest(HttpMethod.Get, "/v1/search") {
                    addHeader("Authorization", "Bearer $TEST_VALID_TOKEN")
                }
            ) {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)

                assertThat(response.content).isNotEmpty()
                val result = json.decodeFromString(SearchUsersResult.serializer(), response.content!!)
                assertThat(result).isEqualTo(aSearchUserResult)
            }
            return@withTestApplication true
        }
    }

    @Test
    fun testSearchInvalidAuthenticated() {
        val fakes = TestAppComponent()
        withTestApplication({
            (environment.config as MapApplicationConfig).setConfig()
            module(fakes.getTestModules(environment.config))
        }) {
            with(
                handleRequest(HttpMethod.Get, "/v1/search") {
                    addHeader("Authorization", "Bearer invalid")
                }
            ) {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }
}
