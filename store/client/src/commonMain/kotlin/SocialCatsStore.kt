package com.nicolasmilliard.socialcats.store

import kotlinx.coroutines.flow.Flow

interface SocialCatsStore {
    suspend fun getCurrentUser(uid: String): Flow<User>
    suspend fun saveInstanceId(userId: String, deviceInfo: DeviceInfo)
}

data class User(
    val id: String,
    val name: String?,
    val createdAt: Long,
    val photoUrl: String?
)

data class DeviceInfo(
    val instanceId: String,
    val token: String,
    val languageTag: String
)
