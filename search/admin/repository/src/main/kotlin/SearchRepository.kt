package com.nicolasmilliard.socialcats.search.repository

import java.util.Date
import mu.KotlinLogging
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.VersionType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

private val log = KotlinLogging.logger {}

interface SearchRepository {
    fun indexUser(indexUser: IndexUser)
    fun searchUsers(input: String): SearchResult
    fun deleteUser(id: String)
}

object SearchConstants {
    object Index {
        object Users {
            const val NAME = "users"
            object Fields {
                const val NAME = "name"
                const val EMAIL = "email"
                const val EMAIL_VERIFIED = "emailVerified"
                const val PHONE_NUMBER = "phoneNumber"
                const val PHOTO_URL = "photoUrl"
                val ALL = setOf(
                    NAME,
                    EMAIL,
                    EMAIL_VERIFIED,
                    PHONE_NUMBER,
                    PHONE_NUMBER,
                    PHOTO_URL)
            }
        }
    }
}

data class IndexUser(
    val id: String,
    val updateTime: Date,
    val fields: Map<String, Any?>
)

data class SearchResult(
    val totalHits: Long,
    val hits: List<Document>
)

data class Document(
    val id: String,
    val fields: Map<String, Any?>
)

class ElasticSearchRepository(
    private val esClient: RestHighLevelClient
) : SearchRepository {

    override fun indexUser(indexUser: IndexUser) =
        measureTimeMillis({ log.debug { "Indexing name took $it ms" } }) {
            log.info { "Updating User name, id: ${indexUser.id}" }

            val request = IndexRequest(SearchConstants.Index.Users.NAME)
                .id(indexUser.id)
                .versionType(VersionType.EXTERNAL)
                .version(indexUser.updateTime.time)
                .source(indexUser.fields)

            val response = esClient.index(request, RequestOptions.DEFAULT)
            log.debug { "Response: $response" }
        }

    override fun deleteUser(id: String) =
        measureTimeMillis({ log.debug { "Deleting user took $it ms" } }) {
            log.info { "Deleting User, id: $id" }
            val request = DeleteRequest(SearchConstants.Index.Users.NAME, id)

            val response = esClient.delete(request, RequestOptions.DEFAULT)
            log.debug { "Response: $response" }
        }

    override fun searchUsers(input: String): SearchResult =
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
            val response = esClient.search(searchRequest, RequestOptions.DEFAULT)
            log.debug { "Response: $response" }

            val hitCount = response.hits.totalHits.value

            return SearchResult(
                hitCount,
                response.hits.map { Document(it.id, it.sourceAsMap) }
            )
        }
}
