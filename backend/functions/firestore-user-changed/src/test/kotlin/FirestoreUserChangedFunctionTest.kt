package com.nicolasmilliard.socialcats

import com.google.cloud.functions.Context
import com.google.common.truth.Truth.assertThat
import com.nicolasmilliard.socialcats.payment.FakePaymentProcessor
import com.nicolasmilliard.socialcats.payment.StripePayments
import com.nicolasmilliard.socialcats.search.SearchUseCase
import com.nicolasmilliard.socialcats.search.repository.FakeSearchRepository
import com.nicolasmilliard.socialcats.store.FakeUserStoreAdmin
import mu.KotlinLogging
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.junit.Test

private val log = KotlinLogging.logger {}

internal class FirestoreUserChangedFunctionTest {

    private class TestComponent {
        val fakeSearchRepository = FakeSearchRepository()
        val searchUseCase = SearchUseCase(fakeSearchRepository)
        val fakePaymentProcessor = FakePaymentProcessor()
        val fakeUserStoreAdmin = FakeUserStoreAdmin()

        fun build(): Graph {
            val appModule = AppModule()
            val moshi = appModule.provideMoshi()
            return Graph(
                searchUseCase,
                RestHighLevelClient(RestClient.builder(HttpHost.create("http://test.com"))),
                moshi,
                lazy { StripePayments(fakePaymentProcessor, fakeUserStoreAdmin) }
            )
        }
    }

    @Test
    fun `on user name updated`() {
        val json =
            """
            {
              "oldValue": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"name": {"stringValue": "OldName"}}},
              "value": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"name": {"stringValue": "NewName"}}},
              "updateMask": {"fieldPaths":["name"]}
            }
            """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        FirestoreUserChangedFunction(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeSearchRepository.indexedUsers).containsKey("id")
    }

    @Test
    fun `on unsupported field update`() {
        val json =
            """
            {
              "oldValue": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"toto": {"stringValue": "OldName"}}},
              "value": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"toto": {"stringValue": "NewName"}}},
              "updateMask": {"fieldPaths":["toto"]}
            }
            """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        FirestoreUserChangedFunction(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeSearchRepository.indexedUsers).isEmpty()
    }

    @Test
    fun `on new user created`() {
        val json =
            """
            {
              "oldValue": {"createTime": null, "updateTime": null, "name": null, "fields": null},
              "value": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"name": {"stringValue": "NewName"}, "email": {"stringValue": "f@f.c"}}},
              "updateMask": null
            }
            """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        FirestoreUserChangedFunction(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeSearchRepository.indexedUsers).hasSize(1)
        assertThat(testComponent.fakeUserStoreAdmin.insertedUsers).hasSize(0)
        assertThat(testComponent.fakeUserStoreAdmin.users["id"]).isNotEmpty()
    }

    @Test
    fun `on user deleted`() {
        val json =
            """
            {
              "oldValue": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"name": {"stringValue": "OldName"}}},
              "value": {"createTime": null, "updateTime": null, "name": null, "fields": null},
              "updateMask": null
            }
            """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        FirestoreUserChangedFunction(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeSearchRepository.deletedUsers).hasSize(1)
    }

    @Test
    fun `test Delete Empty Json`() {
        val json =
            """
{
	"oldValue": {
		"createTime": "2020-05-14T16:56:18.588207Z",
		"fields": {
			"createdAt": {
				"timestampValue": "2020-05-14T16:56:18.519Z"
			},
			"email": {
				"stringValue": "niqo01@gmail.com"
			},
			"emailVerified": {
				"booleanValue": false
			},
			"name": {
				"stringValue": "Nicolas Milliard"
			},
			"phoneNumber": {},
			"photoUrl": {
				"stringValue": "https://lh3.googleusercontent.com/a-/AOh14GhQMPFX-xnquD6iztU29XR9EHOYtDxTO-0uBNzNvQ\u003ds96-c"
			}
		},
		"name": "projects/sweat-monkey/databases/(default)/documents/users/HnEhkgV7qbXiK9S0TCqh9mIibMD3",
		"updateTime": "2020-05-21T21:40:53.760367Z"
	},
	"updateMask": {},
	"value": {}
}
        """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        FirestoreUserChangedFunction(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeSearchRepository.deletedUsers).hasSize(1)
    }

    class FakeContext : Context {
        override fun timestamp() = "timestamp"

        override fun eventId() = "eventId"

        override fun resource() = "resource"

        override fun eventType() = "eventType"
    }
}
