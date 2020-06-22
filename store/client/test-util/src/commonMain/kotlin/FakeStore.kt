package com.nicolasmilliard.socialcats.store

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class FakeStore : UserStore {

    private val storeUserChannel = MutableStateFlow<User?>(null)
    private val userFlow = storeUserChannel.filterNotNull()

    var user: User? = null

    var savedUserId: String? = null
    var savedDeviceInfo: DeviceInfo? = null

    override suspend fun user(uid: String, cacheOnly: Boolean): User? {
        return user
    }

    override suspend fun user(uid: String): Flow<User> {
        return userFlow
    }

    override suspend fun saveDeviceInfo(userId: String, deviceInfo: DeviceInfo) {
        savedUserId = userId
        savedDeviceInfo = deviceInfo
    }

    override suspend fun deviceInfo(userId: String, instanceId: String, cacheOnly: Boolean): DeviceInfo? {
        return savedDeviceInfo
    }

    override suspend fun waitForPendingWrites() {
    }

    fun offer(user: User) {
        this.user = user
        storeUserChannel.value = user
    }
}

val aStoreUser = User("id", "name", 1L, "photo", false)
val aDeviceInfo = DeviceInfo("id", "token", "fr")
