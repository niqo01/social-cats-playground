package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.api.bearer
import com.nicolasmilliard.socialcats.model.SearchUsersResult
import com.nicolasmilliard.socialcats.util.IO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class SearchLoader(
    private val searchService: SearchService
) {
    fun searchUsers(authToken: String, input: String?) = flow {
        emit(Status.InProgress)
        try {
            val bearer = bearer(authToken)
            val searchUsers = withContext(Dispatchers.IO()) { searchService.searchUsers(bearer, input) }
            emit(Status.Success(searchUsers))
        } catch (exception: Throwable) {
            emit(Status.Failure(exception))
        }
    }

    sealed class Status {
        object InProgress : Status()
        data class Success(val data: SearchUsersResult) : Status()
        data class Failure(val exception: Throwable) : Status()
    }
}
