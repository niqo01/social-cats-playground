package com.nicolasmilliard.socialcats.ui

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest.Builder
import com.nicolasmilliard.socialcats.NetworkManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class AndroidNetworkManager(private val connectivityManager: ConnectivityManager) : NetworkManager {
    override fun listenNetworkStatus(): Flow<Boolean> = callbackFlow {
        val connectivityCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                logger.info { "AndroidNetworkManager network onAvailable" }
                offer(true)
            }

            override fun onLost(network: Network) {
                logger.info { "AndroidNetworkManager network onLost" }
                offer(false)
            }
        }
        connectivityManager.registerNetworkCallback(
            Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
            connectivityCallback
        )

        val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        val connected = activeNetworkInfo != null && activeNetworkInfo.isConnected
        offer(connected)
        awaitClose {
            connectivityManager.unregisterNetworkCallback(connectivityCallback)
        }
    }.conflate()
}
