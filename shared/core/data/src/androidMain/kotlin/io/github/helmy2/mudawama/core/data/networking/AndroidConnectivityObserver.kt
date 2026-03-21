package io.github.helmy2.mudawama.core.data.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import io.github.helmy2.mudawama.core.domain.ConnectivityObserver
import io.github.helmy2.mudawama.core.domain.ConnectivityStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class AndroidConnectivityObserver(
    private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val status: Flow<ConnectivityStatus>
        get() = callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    trySend(ConnectivityStatus.Available)
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    trySend(ConnectivityStatus.Losing)
                }

                override fun onLost(network: Network) {
                    trySend(ConnectivityStatus.Lost)
                }

                override fun onUnavailable() {
                    trySend(ConnectivityStatus.Unavailable)
                }
            }

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, callback)

            val currentNetwork = connectivityManager.activeNetwork
            if (currentNetwork == null) {
                trySend(ConnectivityStatus.Unavailable)
            }

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
}
