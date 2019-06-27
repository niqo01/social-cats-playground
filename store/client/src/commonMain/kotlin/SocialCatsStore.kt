package com.nicolasmilliard.socialcats.store

import kotlinx.coroutines.flow.Flow

interface SocialCatsStore {
    suspend fun getCurrentUser(uid: String, cacheOnly: Boolean): User?
    suspend fun getCurrentUser(uid: String): Flow<User>
    suspend fun saveDeviceInfo(userId: String, deviceInfo: DeviceInfo)
    suspend fun getDeviceInfo(userId: String, instanceId: String, cacheOnly: Boolean): DeviceInfo?

    suspend fun waitForPendingWrites()
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
