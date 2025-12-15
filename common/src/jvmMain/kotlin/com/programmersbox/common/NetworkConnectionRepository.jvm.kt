package com.programmersbox.common

import dev.jordond.connectivity.Connectivity

actual fun createConnectivity(): Connectivity = Connectivity {
    autoStart = true
}