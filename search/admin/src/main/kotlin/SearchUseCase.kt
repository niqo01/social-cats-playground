package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.model.SearchUsersResult
import com.nicolasmilliard.socialcats.model.User
import com.nicolasmilliard.socialcats.search.repository.IndexUser
import com.nicolasmilliard.socialcats.search.repository.SearchConstants.Index.Users
import com.nicolasmilliard.socialcats.search.repository.SearchRepository
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class SearchUseCase(
    private val repository: SearchRepository
) {

    fun indexUser(indexUser: IndexUser) {
        log.info { "Indexing New User name, id: $indexUser.id" }
        require(
            indexUser.fields.containsKey(Users.Fields.NAME) ||
                indexUser.fields.containsKey(Users.Fields.EMAIL) ||
                indexUser.fields.containsKey(Users.Fields.PHONE_NUMBER)
        )
        indexUser.fields.keys.forEach { require(it in Users.Fields.ALL) }
        repository.indexUser(indexUser)
    }

    fun deleteUser(id: String) {
        log.info { "Deleting User, id: $id" }
        repository.deleteUser(id)
    }

    fun searchUsers(callerUid: String?, input: String): SearchUsersResult {
        log.info { "Searching Users, input: $input" }
        val searchResults = repository.searchUsers(input)
        val users = searchResults.hits.map {
            val name = it.fields[Users.Fields.NAME] as String?
            val email = it.fields[Users.Fields.EMAIL] as String?
            val phoneNumber = it.fields[Users.Fields.PHONE_NUMBER] as String?
            val displayName = when {
                !name.isNullOrBlank() -> name
                !email.isNullOrBlank() -> email
                else -> phoneNumber
            } as String
            val photoUrl = it.fields[Users.Fields.PHOTO_URL] as String?
            User(it.id, displayName, photoUrl)
        }
        return SearchUsersResult(searchResults.totalHits, users)
    }
}
