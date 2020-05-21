package com.nicolasmilliard.socialcats.searchapi

import com.google.common.truth.Truth.assertThat
import com.nicolasmilliard.socialcats.payment.CancelSubscriptionResult
import com.nicolasmilliard.socialcats.payment.CreateSubscriptionRequest
import com.nicolasmilliard.socialcats.payment.CreateSubscriptionResult
import com.nicolasmilliard.socialcats.payment.Invoice
import com.nicolasmilliard.socialcats.payment.PaymentStatus
import com.nicolasmilliard.socialcats.payment.Subscription
import com.nicolasmilliard.socialcats.payment.SubscriptionDetailResult
import com.nicolasmilliard.socialcats.payment.SubscriptionStatus
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import org.koin.test.AutoCloseKoinTest

@OptIn(ExperimentalStdlibApi::class)
class PaymentTest : AutoCloseKoinTest() {

    @Test
    fun testGetSubscriptionDetail() {
        val fakes = TestAppComponent()
        withTestApplication({
            (environment.config as MapApplicationConfig).setConfig()
            module(fakes.getTestModules(environment.config))
        }) {
            with(handleRequest(HttpMethod.Get, "/v1/payments/subscriptionDetail") {
                addHeader("Authorization", "Bearer $TEST_VALID_TOKEN")
                addHeader("Content-type", "application/json")
            }) {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isNotEmpty()
                val result = json.parse(SubscriptionDetailResult.serializer(), response.content!!)
                assertThat(result.prices).hasSize(2)
            }
        }
    }

    @Test
    fun testCreateSubscriptionAuthenticated() {
        val fakes = TestAppComponent()
        fakes.userStore.users["uid"] = "customerId"
        fakes.paymentProcessor.subscriptionsCreationResult.add(
            Subscription(
                "id",
                SubscriptionStatus.ACTIVE,
                Invoice("id", PaymentStatus.SUCCEEDED, "dsa")
            )
        )
        withTestApplication({
            (environment.config as MapApplicationConfig).setConfig()
            module(fakes.getTestModules(environment.config))
        }) {
            with(handleRequest(HttpMethod.Post, "/v1/payments/createSubscription") {
                addHeader("Authorization", "Bearer $TEST_VALID_TOKEN")
                addHeader("Content-type", "application/json")
                setBody(
                    json.stringify(
                        CreateSubscriptionRequest.serializer(),
                        CreateSubscriptionRequest("paymentId", "priceId")
                    )
                )
            }) {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isNotEmpty()
                val result = json.parse(CreateSubscriptionResult.serializer(), response.content!!)
                assertThat(result.subscription.invoice!!.paymentStatus).isEqualTo(PaymentStatus.SUCCEEDED)
                assertThat(fakes.userStore.membershipStatusChanged).containsEntry("uid", true)
            }
        }
    }

    @Test
    fun testSubscriptionCanceled() {
        val fakes = TestAppComponent()
        fakes.userStore.users["uid"] = "customerId"
        fakes.paymentProcessor.subscriptionsCancelResult.add(SubscriptionStatus.CANCELED)
        withTestApplication({
            (environment.config as MapApplicationConfig).setConfig()
            module(fakes.getTestModules(environment.config))
        }) {
            with(handleRequest(HttpMethod.Post, "/v1/payments/cancelSubscription") {
                addHeader("Authorization", "Bearer $TEST_VALID_TOKEN")
                addHeader("Content-type", "application/json")
            }) {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isNotEmpty()
                val result = json.parse(CancelSubscriptionResult.serializer(), response.content!!)
                assertThat(result.subsriptionStatus).isEqualTo(SubscriptionStatus.CANCELED)
            }
        }
    }
}
