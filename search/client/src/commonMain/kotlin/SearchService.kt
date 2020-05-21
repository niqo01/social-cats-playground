package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.model.SearchUsersResult

expect interface SearchService {
    suspend fun searchUsers(authToken: String, query: String?): SearchUsersResult
}
