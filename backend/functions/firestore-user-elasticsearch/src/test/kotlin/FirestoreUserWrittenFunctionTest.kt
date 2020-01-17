package com.nicolasmilliard.socialcats

import com.google.cloud.functions.Context
import com.google.common.truth.Truth.assertThat
import com.nicolasmilliard.socialcats.search.SearchUseCase
import com.nicolasmilliard.socialcats.search.repository.IndexUser
import com.nicolasmilliard.socialcats.search.repository.SearchRepository
import com.nicolasmilliard.socialcats.search.repository.SearchResult
import mu.KotlinLogging
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.junit.Test

private val log = KotlinLogging.logger {}

internal class FirestoreEventFunctionTest {

    private class TestComponent {
        val fakeSearchUseCase = FakeSearchRepository()
        val searchUseCase = SearchUseCase(fakeSearchUseCase)

        fun build(): Graph {
            val appModule = AppModule()
            val moshi = appModule.provideMoshi()
            return Graph(
                searchUseCase,
                RestHighLevelClient(RestClient.builder(HttpHost.create("http://test.com"))),
                moshi
            )
        }
    }

    @Test
    fun `on user name updated`() {
        val json = """
            {
              "oldValue": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"name": {"stringValue": "OldName"}}},
              "value": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"name": {"stringValue": "NewName"}}},
              "updateMask": {"fieldPaths":["name"]}
            }
        """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        FirestoreUserWrittenFunction(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeSearchUseCase.wasUpdatedCalled).isTrue()
    }

    @Test
    fun `on unsupported field update`() {
        val json = """
            {
              "oldValue": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"toto": {"stringValue": "OldName"}}},
              "value": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"toto": {"stringValue": "NewName"}}},
              "updateMask": {"fieldPaths":["toto"]}
            }
        """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        FirestoreUserWrittenFunction(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeSearchUseCase.wasUpdatedCalled).isFalse()
    }

    @Test
    fun `on new user created`() {
        val json = """
            {
              "oldValue": {"createTime": null, "updateTime": null, "name": null, "fields": null},
              "value": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"name": {"stringValue": "NewName"}}},
              "updateMask": null
            }
        """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        FirestoreUserWrittenFunction(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeSearchUseCase.wasUpdatedCalled).isTrue()
    }

    @Test
    fun `on new user deleted`() {
        val json = """
            {
              "oldValue": {"createTime": "2002-10-02T10:00:00-05:00", "updateTime": "2002-10-02T10:00:00-05:00", "name": "name/id", "fields": {"name": {"stringValue": "OldName"}}},
              "value": {"createTime": null, "updateTime": null, "name": null, "fields": null},
              "updateMask": null
            }
        """.trimIndent()

        val context = FakeContext()
        val testComponent = TestComponent()
        FirestoreUserWrittenFunction(testComponent.build()).accept(json, context)

        assertThat(testComponent.fakeSearchUseCase.wasDeletedCalled).isTrue()
    }

    class FakeContext : Context {
        override fun timestamp() = "timestamp"

        override fun eventId() = "eventId"

        override fun resource() = "resource"

        override fun eventType() = "eventType"
    }

    class FakeSearchRepository : SearchRepository {
        override fun indexUser(indexUser: IndexUser) {
            log.debug { "updateUserName()" }
            if (wasUpdatedCalled) IllegalStateException("updateUserName called more than once")
            wasUpdatedCalled = true
        }

        override fun searchUsers(input: String): SearchResult {
            TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
        }

        var wasDeletedCalled: Boolean = false
        override fun deleteUser(id: String) {
            log.debug { "deleteUser()" }
            if (wasDeletedCalled) IllegalStateException("updateUserName called more than once")
            wasDeletedCalled = true
        }

        var wasUpdatedCalled: Boolean = false
    }
}
