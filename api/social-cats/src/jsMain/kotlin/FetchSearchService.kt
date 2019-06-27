package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.model.SearchUsersResult
import com.nicolasmilliard.socialcats.util.IoException
import kotlin.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.dom.url.URL
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response

object FetchSearchService : SearchService {

    private val listUrl = URL("list", PRODUCTION_API).href

    override suspend fun searchUsers(authToken: String, query: String?): SearchUsersResult {
        val authHeader: dynamic = object {}
        authHeader[AUTHORIZATION_HEADER] = "$AUTHORIZATION_SCHEME $authToken"
        return window
            .fetch(
                listUrl,
                RequestInit(headers = authHeader)
            )
            .catch<Response> {
                throw IoException(null, it)
            }
            .then {
                if (!it.ok) {
                    throw RuntimeException("HTTP ${it.status} ${it.statusText}")
                } else {
                    it.text()
                }
            }.then {
                Json.parse(SearchUsersResult.serializer(), it)
            }
            .await()
    }
}
