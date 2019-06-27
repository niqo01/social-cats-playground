package com.nicolasmilliard.socialcats.store

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class FakeStore : SocialCatsStore {

    private val storeChannel: BroadcastChannel<User> = ConflatedBroadcastChannel()
    private val flow = storeChannel.asFlow()

    var savedUserId: String? = null
    var savedDeviceInfo: DeviceInfo? = null

    override suspend fun getCurrentUser(uid: String): Flow<User> {
        return flow
    }

    override suspend fun saveInstanceId(userId: String, deviceInfo: DeviceInfo) {
        savedUserId = userId
        savedDeviceInfo = deviceInfo
    }

    fun offer(user: User) {
        storeChannel.offer(user)
    }
}

val aStoreUser = User("id", "name", 1L, "photo")
