package io.github.helmy2.mudawama.core.data.networking

import io.github.helmy2.mudawama.core.domain.ConnectivityObserver
import io.github.helmy2.mudawama.core.domain.ConnectivityStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create

class IosConnectivityObserver : ConnectivityObserver {
    override val status: Flow<ConnectivityStatus>
        get() = callbackFlow {
            val monitor = nw_path_monitor_create()
            
            nw_path_monitor_set_update_handler(monitor) { path ->
                val status = nw_path_get_status(path)
                if (status == nw_path_status_satisfied) {
                    trySend(ConnectivityStatus.Available)
                } else {
                    trySend(ConnectivityStatus.Unavailable)
                }
            }
            
            val queue = dispatch_queue_create("InternetConnectionMonitor", null)
            nw_path_monitor_set_queue(monitor, queue)
            nw_path_monitor_start(monitor)
            
            awaitClose {
                nw_path_monitor_cancel(monitor)
            }
        }.distinctUntilChanged()
}
