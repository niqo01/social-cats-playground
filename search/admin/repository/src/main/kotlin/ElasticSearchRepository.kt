package com.nicolasmilliard.socialcats.search.repository

import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.VersionType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

private val log = KotlinLogging.logger {}

class ElasticSearchRepository(
    private val esClient: RestHighLevelClient
) : SearchRepository {

    override suspend fun indexUser(indexUser: IndexUser) =
        measureTimeMillis({ log.debug { "Indexing name took $it ms" } }) {
            log.info { "Updating User name, id: ${indexUser.id}" }

            val request = IndexRequest(SearchConstants.Index.Users.NAME)
                .id(indexUser.id)
                .versionType(VersionType.EXTERNAL)
                .version(indexUser.updateTime.time)
                .source(indexUser.fields)

            val response = index(request, RequestOptions.DEFAULT)
            log.debug { "Response: $response" }
        }

    override suspend fun deleteUser(id: String) =
        measureTimeMillis({ log.debug { "Deleting user took $it ms" } }) {
            log.info { "Deleting User, id: $id" }
            val request = DeleteRequest(SearchConstants.Index.Users.NAME, id)

            val response = delete(request, RequestOptions.DEFAULT)
            log.debug { "Response: $response" }
        }

    override suspend fun searchUsers(input: String): SearchResult =
        measureTimeMillis({ log.debug { "Search user took $it ms" } }) {
            log.info { "Searching Users, input: $input" }
            val searchSourceBuilder = SearchSourceBuilder()
                .apply {
                    if (input.isNullOrBlank()) {
                        query(QueryBuilders.matchAllQuery())
                    } else {
                        query(QueryBuilders.matchQuery("name", input))
                    }
                }

            val searchRequest = SearchRequest(SearchConstants.Index.Users.NAME).source(searchSourceBuilder)
            val response = search(searchRequest, RequestOptions.DEFAULT)
            log.debug { "Response: $response" }

            val hitCount = response.hits.totalHits!!.value

            return SearchResult(
                hitCount,
                response.hits.map { Document(it.id, it.sourceAsMap) }
            )
        }

    private suspend fun index(
        indexRequest: IndexRequest,
        options: RequestOptions
    ) = suspendCancellableCoroutine<IndexResponse> {
        val cancellable = esClient.indexAsync(indexRequest, options, object : ActionListener<IndexResponse> {
            override fun onFailure(e: Exception) {
                it.resumeWithException(e)
            }

            override fun onResponse(response: IndexResponse) {
                it.resume(response)
            }
        })
        it.invokeOnCancellation {
            cancellable.cancel()
        }
    }

    private suspend fun delete(
        deleteRequest: DeleteRequest,
        options: RequestOptions
    ) = suspendCancellableCoroutine<DeleteResponse> {
        val cancellable = esClient.deleteAsync(deleteRequest, options, object : ActionListener<DeleteResponse> {
            override fun onFailure(e: Exception) {
                it.resumeWithException(e)
            }

            override fun onResponse(response: DeleteResponse) {
                it.resume(response)
            }
        })
        it.invokeOnCancellation {
            cancellable.cancel()
        }
    }

    private suspend fun search(
        searchRequest: SearchRequest,
        options: RequestOptions
    ) = suspendCancellableCoroutine<SearchResponse> {
        val cancellable = esClient.searchAsync(searchRequest, options, object : ActionListener<SearchResponse> {
            override fun onFailure(e: Exception) {
                it.resumeWithException(e)
            }

            override fun onResponse(response: SearchResponse) {
                it.resume(response)
            }
        })
        it.invokeOnCancellation {
            cancellable.cancel()
        }
    }
}
