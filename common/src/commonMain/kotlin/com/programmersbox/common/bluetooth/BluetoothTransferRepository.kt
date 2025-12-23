package com.programmersbox.common.bluetooth

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow

data class BluetoothDevice(
    val name: String,
    val address: String
)

interface BluetoothTransferRepository {
    fun getPairedDevices(): Flow<List<BluetoothDevice>>
    suspend fun sendFile(device: BluetoothDevice, file: PlatformFile): Result<Unit>
    suspend fun sendFavorites(device: BluetoothDevice): Result<Unit>
    suspend fun sendBlacklisted(device: BluetoothDevice): Result<Unit>
    suspend fun sendLists(device: BluetoothDevice, uuids: List<String>): Result<Unit>
    fun listenForFiles(): Flow<IncomingFile>
}

data class IncomingFile(
    val name: String,
    val content: ByteArray
)
