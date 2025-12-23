package com.programmersbox.common.bluetooth

import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.ListDao
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class JvmBluetoothTransferRepository(
    private val favoritesDao: FavoritesDao,
    private val listDao: ListDao,
) : BluetoothTransferRepository {

    private val MY_UUID =
        "0000110100001000800000805F9B34FB" // Standard SerialPortService ID (without hyphens for JSR-82)

    private val json by lazy {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    override fun getPairedDevices(): Flow<List<BluetoothDevice>> {
        TODO("Not yet implemented")
    }

    override suspend fun sendFile(
        device: BluetoothDevice,
        file: PlatformFile
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun sendFavorites(device: BluetoothDevice): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun sendBlacklisted(device: BluetoothDevice): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun sendLists(
        device: BluetoothDevice,
        uuids: List<String>
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun listenForFiles(): Flow<IncomingFile> {
        TODO("Not yet implemented")
    }


}
