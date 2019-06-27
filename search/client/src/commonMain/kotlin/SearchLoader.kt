package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.model.SearchUsersResult
import kotlinx.coroutines.flow.flow

class SearchLoader(
    private val searchService: SearchService
) {
    fun searchUsers(authToken: String, input: String?) = flow {
        emit(Status.InProgress)
        try {
            val bearer = "$AUTHORIZATION_SCHEME $authToken"
            val searchUsers = searchService.searchUsers(bearer, input)
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
