package com.nicolasmilliard.socialcats.store

import kotlinx.coroutines.flow.Flow

interface SocialCatsStore {
    fun getCurrentUser(uid: String): Flow<User>
}

data class User(
    val id: String,
    val name: String?,
    val createdAt: Long,
    val photoUrl: String?
)
