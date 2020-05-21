package com.nicolasmilliard.socialcats.store

import kotlinx.coroutines.flow.Flow

interface UserStore {
    suspend fun user(uid: String, cacheOnly: Boolean): User?
    suspend fun user(uid: String): Flow<User>
    suspend fun deviceInfo(userId: String, instanceId: String, cacheOnly: Boolean): DeviceInfo?
    suspend fun saveDeviceInfo(userId: String, deviceInfo: DeviceInfo)

    suspend fun waitForPendingWrites()
}

data class User(
    val id: String,
    val name: String?,
    val createdAt: Long,
    val photoUrl: String?,
    val isMember: Boolean
)

data class DeviceInfo(
    val instanceId: String,
    val token: String,
    val languageTag: String
)
