package com.nicolasmilliard.socialcats.searchapi

import com.google.common.truth.Truth.assertThat
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import org.koin.test.AutoCloseKoinTest

class ApplicationTest : AutoCloseKoinTest() {

    @Test
    fun testRoot() {
        val fakes = TestAppComponent()
        withTestApplication({
            (environment.config as MapApplicationConfig).setConfig()
            module(fakes.getTestModules(environment.config))
        }) {
            with(handleRequest(HttpMethod.Get, "/")) {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo("Pong")
            }
        }
    }
}
