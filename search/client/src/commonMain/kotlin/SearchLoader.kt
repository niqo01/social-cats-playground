package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.model.SearchUsersResult
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging

class SearchLoader(
    private val searchService: SearchService
) {
    private val logger = KotlinLogging.logger {}

    fun searchUsers(authToken: String?, input: String?) = flow {
        logger.info { "Searching Users" }
        emit(Status.InProgress)
        try {
            val bearer = if (authToken != null) "$AUTHORIZATION_SCHEME $authToken" else null
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
