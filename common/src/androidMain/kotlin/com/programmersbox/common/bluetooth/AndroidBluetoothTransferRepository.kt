package com.programmersbox.common.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.ListDao
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.UUID

class AndroidBluetoothTransferRepository(
    context: Context,
    private val favoritesDao: FavoritesDao,
    private val listDao: ListDao,
) : BluetoothTransferRepository {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val MY_UUID: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID

    private val json by lazy {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @SuppressLint("MissingPermission")
    override fun getPairedDevices(): Flow<List<BluetoothDevice>> = flow {
        val pairedDevices = bluetoothAdapter
            ?.bondedDevices
            ?.map { BluetoothDevice(it.name ?: "Unknown", it.address) }
            ?: emptyList()
        emit(pairedDevices)
    }
        .flowOn(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    override suspend fun sendFile(
        device: BluetoothDevice,
        file: PlatformFile
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val remoteDevice = bluetoothAdapter?.getRemoteDevice(device.address)
            ?: return@withContext Result.failure(Exception("Device not found"))

        var socket: BluetoothSocket? = null
        try {
            socket = remoteDevice.createRfcommSocketToServiceRecord(MY_UUID)
            socket.connect()

            val outputStream = socket.outputStream
            val bytes = file.readBytes()

            // Send file name length, then name, then file size, then content
            val fileName = file.name
            val fileNameBytes = fileName.toByteArray()
            outputStream.write(fileNameBytes.size)
            outputStream.write(fileNameBytes)

            val fileSize = bytes.size
            outputStream.write((fileSize shr 24) and 0xff)
            outputStream.write((fileSize shr 16) and 0xff)
            outputStream.write((fileSize shr 8) and 0xff)
            outputStream.write(fileSize and 0xff)

            outputStream.write(bytes)
            outputStream.flush()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            socket?.close()
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun sendFavorites(device: BluetoothDevice): Result<Unit> {
        return sendObject(
            device = device,
            name = "favorites.json",
            obj = json.encodeToString(favoritesDao.exportFavorites(json))
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun sendBlacklisted(device: BluetoothDevice): Result<Unit> {
        return sendObject(
            device = device,
            name = "blacklisted.json",
            obj = json.encodeToString(favoritesDao.exportBlacklisted())
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun sendLists(device: BluetoothDevice, uuids: List<String>): Result<Unit> {
        return sendObject(
            device = device,
            name = "lists.json",
            obj = json.encodeToString(listDao.getAllListItems(*uuids.toTypedArray()))
        )
    }

    private suspend fun sendObject(
        device: BluetoothDevice,
        name: String,
        obj: String
    ) = withContext(Dispatchers.IO) {
        val remoteDevice = bluetoothAdapter?.getRemoteDevice(device.address)
            ?: return@withContext Result.failure(Exception("Device not found"))

        var socket: BluetoothSocket? = null
        try {
            socket = remoteDevice.createRfcommSocketToServiceRecord(MY_UUID)
            socket.connect()

            val outputStream = socket.outputStream
            val bytes = obj.byteInputStream().readBytes()
            // Send file name length, then name, then file size, then content
            val fileName = name
            val fileNameBytes = fileName.toByteArray()
            outputStream.write(fileNameBytes.size)
            outputStream.write(fileNameBytes)

            val fileSize = bytes.size
            outputStream.write((fileSize shr 24) and 0xff)
            outputStream.write((fileSize shr 16) and 0xff)
            outputStream.write((fileSize shr 8) and 0xff)
            outputStream.write(fileSize and 0xff)

            outputStream.write(bytes)
            outputStream.flush()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            socket?.close()
        }
    }

    @SuppressLint("MissingPermission")
    override fun listenForFiles(): Flow<IncomingFile> = flow {
        val serverSocket: BluetoothServerSocket? = try {
            bluetoothAdapter?.listenUsingRfcommWithServiceRecord("CivitModelBrowser", MY_UUID)
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        while (true) {
            var socket: BluetoothSocket? = null
            try {
                socket = serverSocket?.accept()
                if (socket != null) {
                    val inputStream = socket.inputStream

                    val fileNameLength = inputStream.read()
                    if (fileNameLength == -1) continue

                    val fileNameBytes = ByteArray(fileNameLength)
                    inputStream.read(fileNameBytes)
                    val fileName = String(fileNameBytes)
                    println("File name: $fileName")

                    val b1 = inputStream.read() and 0xff
                    val b2 = inputStream.read() and 0xff
                    val b3 = inputStream.read() and 0xff
                    val b4 = inputStream.read() and 0xff
                    val fileSize = (b1 shl 24) or (b2 shl 16) or (b3 shl 8) or b4

                    val content = ByteArray(fileSize)
                    var bytesRead = 0
                    while (bytesRead < fileSize) {
                        val result = inputStream.read(content, bytesRead, fileSize - bytesRead)
                        if (result == -1) break
                        bytesRead += result
                    }

                    println("File content: ${String(content)}")

                    emit(IncomingFile(fileName, content))
                }
            } catch (e: IOException) {
                break
            } finally {
                socket?.close()
            }
        }
        serverSocket?.close()
    }
        .flowOn(Dispatchers.IO)
}
