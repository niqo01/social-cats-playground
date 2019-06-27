package com.nicolasmilliard.socialcats

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ConnectivityChecker(val networkManager: NetworkManager) {
    private val _connectedStatus = ConflatedBroadcastChannel<Boolean>()
    val connectedStatus: Flow<Boolean> get() = _connectedStatus.asFlow()

    suspend fun start() {
        coroutineScope {
            launch {
                networkManager.listenNetworkStatus()
                    .distinctUntilChanged()
                    .collect {
                        _connectedStatus.offer(it)
                    }
            }
        }
    }
}

interface NetworkManager {
    fun listenNetworkStatus(): Flow<Boolean>
}
