package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.model.SearchUsersResult
import com.nicolasmilliard.socialcats.model.User
import com.nicolasmilliard.socialcats.search.repository.IndexUser
import com.nicolasmilliard.socialcats.search.repository.SearchConstants.Index.Users
import com.nicolasmilliard.socialcats.search.repository.SearchRepository
import com.nicolasmilliard.socialcats.store.DbConstants
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class SearchUseCase(
    private val repository: SearchRepository
) {

    suspend fun indexUser(value: FirestoreValue) {
        val indexFields = mutableMapOf<String, Any?>()

        value.fields.forEach { (k, v) ->
            when (k) {
                DbConstants.Collections.Users.Fields.NAME -> indexFields[Users.Fields.NAME] = v
                DbConstants.Collections.Users.Fields.PHOTO_URL -> indexFields[Users.Fields.PHOTO_URL] = v
                DbConstants.Collections.Users.Fields.EMAIL_VERIFIED -> indexFields[Users.Fields.EMAIL_VERIFIED] = v
                DbConstants.Collections.Users.Fields.EMAIL -> indexFields[Users.Fields.EMAIL] = v
                DbConstants.Collections.Users.Fields.PHONE_NUMBER -> indexFields[Users.Fields.PHONE_NUMBER] = v
                else -> log.warn { "Ignoring field: $k" }
            }
        }

        if (indexFields.isEmpty()) {
            log.info { "This user change is not interesting for us: ${value.fields.keys.joinToString()}}" }
        } else {
            val indexUser = IndexUser(value.resourceId, value.createTime, indexFields)
            log.info { "Indexing New User name, id: $indexUser.id" }
            require(
                indexUser.fields.containsKey(Users.Fields.NAME) ||
                    indexUser.fields.containsKey(Users.Fields.EMAIL) ||
                    indexUser.fields.containsKey(Users.Fields.PHONE_NUMBER)
            )
            indexUser.fields.keys.forEach { require(it in Users.Fields.ALL) }
            repository.indexUser(indexUser)
        }
    }

    suspend fun deleteUser(id: String) {
        log.info { "Deleting User, id: $id" }
        repository.deleteUser(id)
    }

    suspend fun searchUsers(callerUid: String?, input: String): SearchUsersResult {
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
