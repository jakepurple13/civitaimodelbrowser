package com.programmersbox.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.jordond.connectivity.Connectivity
import kotlinx.coroutines.flow.onEach

class NetworkConnectionRepository {
    val connectivity: Connectivity = createConnectivity()
    var shouldShowMedia: Boolean by mutableStateOf(false)

    fun start() {
        connectivity.start()
    }

    fun stop() {
        connectivity.stop()
    }
}

fun NetworkConnectionRepository.connectivityFlow() = connectivity
    .statusUpdates
    .onEach {
        shouldShowMedia = when (it) {
            is Connectivity.Status.Connected -> !it.isMetered
            Connectivity.Status.Disconnected -> false
        }
    }

expect fun createConnectivity(): Connectivity