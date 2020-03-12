package com.nicolasmilliard.socialcats.search.repository

import java.util.Date
import mu.KotlinLogging

interface SearchRepository {
    suspend fun indexUser(indexUser: IndexUser)
    suspend fun searchUsers(input: String): SearchResult
    suspend fun deleteUser(id: String)
}

object SearchConstants {
    object Index {
        object Users {
            const val NAME = "users"
            object Fields {
                const val NAME = "name"
                const val EMAIL = "email"
                const val EMAIL_VERIFIED = "emailVerified"
                const val PHONE_NUMBER = "phoneNumber"
                const val PHOTO_URL = "photoUrl"
                val ALL = setOf(
                    NAME,
                    EMAIL,
                    EMAIL_VERIFIED,
                    PHONE_NUMBER,
                    PHONE_NUMBER,
                    PHOTO_URL)
            }
        }
    }
}

data class IndexUser(
    val id: String,
    val updateTime: Date,
    val fields: Map<String, Any?>
)

data class SearchResult(
    val totalHits: Long,
    val hits: List<Document>
)

data class Document(
    val id: String,
    val fields: Map<String, Any?>
)

