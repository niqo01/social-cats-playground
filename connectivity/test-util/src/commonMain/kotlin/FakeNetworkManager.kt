package com.nicolasmilliard.socialcats

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class FakeNetworkManager : NetworkManager {
    private val networkChannel: BroadcastChannel<Boolean> = ConflatedBroadcastChannel()
    private val flow: Flow<Boolean> = networkChannel.asFlow()

    override fun listenNetworkStatus(): Flow<Boolean> {
        return flow
    }

    fun offer(connected: Boolean) {
        networkChannel.offer(connected)
    }
}
