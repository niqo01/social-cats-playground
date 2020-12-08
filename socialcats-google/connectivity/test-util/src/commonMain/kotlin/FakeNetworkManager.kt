package com.nicolasmilliard.socialcats

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class FakeNetworkManager : NetworkManager {
    private val networkChannel = MutableStateFlow<Boolean?>(null)
    private val flow: Flow<Boolean> = networkChannel.filterNotNull()

    override fun listenNetworkStatus(): Flow<Boolean> {
        return flow
    }

    fun offer(connected: Boolean) {
        networkChannel.value = connected
    }
}
