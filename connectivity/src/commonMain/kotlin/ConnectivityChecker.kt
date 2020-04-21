package com.nicolasmilliard.socialcats

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ConnectivityChecker(val networkManager: NetworkManager) {
    private val _connectedStatus = MutableStateFlow<Boolean?>(null)
    val connectedStatus: Flow<Boolean> get() = _connectedStatus.filterNotNull()

    suspend fun start() = coroutineScope {
        logger.info { "Start listening" }
        networkManager.listenNetworkStatus()
            .collect {
                logger.info { "Received: $it" }
                _connectedStatus.value = it
            }
    }
}

interface NetworkManager {
    fun listenNetworkStatus(): Flow<Boolean>
}
