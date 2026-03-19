package com.programmersbox.common

import dev.jordond.connectivity.Connectivity

class NetworkConnectionRepository {
    val connectivity: Connectivity = createConnectivity()
}

expect fun createConnectivity(): Connectivity