package com.nicolasmilliard.socialcats.ui

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest.Builder
import com.nicolasmilliard.socialcats.NetworkManager
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.conflate
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class AndroidNetworkManager(private val connectivityManager: ConnectivityManager) : NetworkManager {
    override fun listenNetworkStatus(): Flow<Boolean> = channelFlow<Boolean> {

        fun onConnectivityChange(network: Network, isOnline: Boolean) {
            val isAnyOnline = connectivityManager.allNetworks.any {
                if (it == network) {
                    // Don't trust the network capabilities for the network that just changed.
                    isOnline
                } else {
                    it.isOnline()
                }
            }
            offer(isAnyOnline)
        }

        val connectivityCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                logger.info { "AndroidNetworkManager network onAvailable" }
                onConnectivityChange(network,true)
            }

            override fun onLost(network: Network) {
                logger.info { "AndroidNetworkManager network onLost" }
                onConnectivityChange(network,false)
            }
        }
        connectivityManager.registerNetworkCallback(
            Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
            connectivityCallback
        )

        awaitClose {
            connectivityManager.unregisterNetworkCallback(connectivityCallback)
        }
    }.conflate()

    private fun Network.isOnline(): Boolean {
        val capabilities: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(this)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
