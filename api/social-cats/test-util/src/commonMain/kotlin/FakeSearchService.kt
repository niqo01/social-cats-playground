package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.model.SearchUsersResult
import com.nicolasmilliard.socialcats.model.User

class FakeSearchService : SearchService {
    val result: SearchUsersResult = aSearchResult

    override suspend fun searchUsers(authToken: String?, query: String?): SearchUsersResult {
        return result
    }
}

val aUser = User("id", "name")

val aSearchResult = SearchUsersResult(
    1,
    listOf(aUser)
)
