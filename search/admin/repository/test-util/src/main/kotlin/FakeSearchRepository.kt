package com.nicolasmilliard.socialcats.search.repository

import com.nicolasmilliard.socialcats.model.SearchUsersResult
import com.nicolasmilliard.socialcats.model.User
import java.util.Date

val aSearchResultNoInput = SearchUsersResult(3, listOf(User("id", "name"), User("id1", "name1"), User("id2", "name2")))
val aSearchResult = SearchUsersResult(2, listOf(User("id", "name"), User("id1", "name1")))

class FakeSearchRepository() : SearchRepository {

    val updatedUser = HashMap<String, String>()
    val deletedUsers = HashSet<String>()

    override fun updateUserName(id: String, updateTime: Date, name: String) {
        updatedUser[id] = name
    }

    override fun searchUsers(input: String): SearchUsersResult {
        return if (input.isEmpty()) aSearchResultNoInput else aSearchResult
    }

    override fun deleteUser(id: String) {
        deletedUsers.add(id)
    }
}
