package io.github.helmy2.mudawama.core.domain

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    val status: Flow<ConnectivityStatus>
}

sealed interface ConnectivityStatus {
    data object Available : ConnectivityStatus
    data object Unavailable : ConnectivityStatus
    data object Losing : ConnectivityStatus
    data object Lost : ConnectivityStatus
}
