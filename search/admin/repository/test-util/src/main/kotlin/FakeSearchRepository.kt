package com.nicolasmilliard.socialcats.search.repository

val aDocument1 = Document(
    "id",
    mapOf(
        SearchConstants.Index.Users.Fields.NAME to "name",
        SearchConstants.Index.Users.Fields.PHOTO_URL to null
    )
)
val aDocument2 = Document(
    "id2",
    mapOf(
        SearchConstants.Index.Users.Fields.NAME to "name2",
        SearchConstants.Index.Users.Fields.PHOTO_URL to "photoUrl2"
    )
)
val aDocument3 = Document(
    "id3",
    mapOf(
        SearchConstants.Index.Users.Fields.PHONE_NUMBER to "415 123 1234",
        SearchConstants.Index.Users.Fields.PHOTO_URL to "photoUrl3"
    )
)

val aSearchResultNoInput = SearchResult(3, listOf(aDocument1, aDocument2, aDocument3))
val aSearchResult = SearchResult(2, listOf(aDocument1, aDocument2))

class FakeSearchRepository() : SearchRepository {

    val updatedUsers = HashMap<String, IndexUser>()
    val deletedUsers = HashSet<String>()

    override fun indexUser(indexUser: IndexUser) {
        updatedUsers[indexUser.id] = indexUser
    }

    override fun searchUsers(input: String): SearchResult {
        return if (input.isEmpty()) aSearchResultNoInput else aSearchResult
    }

    override fun deleteUser(id: String) {
        deletedUsers.add(id)
    }
}
