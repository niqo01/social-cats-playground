package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.model.SearchUsersResult
import com.nicolasmilliard.socialcats.search.repository.SearchRepository
import java.util.Date
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class SearchUseCase(
    private val repository: SearchRepository
) {

    fun updateUserName(id: String, updateTime: Date, name: String) {
        log.info { "Updating User name, id: $id" }
        repository.updateUserName(id, updateTime, name)
    }

    fun deleteUser(id: String) {
        log.info { "Deleting User, id: $id" }
        repository.deleteUser(id)
    }

    fun searchUsers(callerUid: String?, input: String): SearchUsersResult {
        log.info { "Searching Users, input: $input" }
        return repository.searchUsers(input)
    }
}
