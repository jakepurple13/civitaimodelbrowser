package com.programmersbox.common.bluetooth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.common.bluetooth.BluetoothDevice
import com.programmersbox.common.bluetooth.BluetoothTransferRepository
import com.programmersbox.common.bluetooth.IncomingFile
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BluetoothTransferViewModel(
    private val repository: BluetoothTransferRepository
) : ViewModel() {
    val pairedDevices = repository.getPairedDevices()
    var selectedDevice: BluetoothDevice? by mutableStateOf(null)
    var incomingFile: IncomingFile? by mutableStateOf(null)

    init {
        repository.listenForFiles()
            .onEach { println(it) }
            .onEach { println(it.content.decodeToString()) }
            .onEach { incomingFile = it }
            .launchIn(viewModelScope)
    }

    fun sendFile(device: BluetoothDevice, file: PlatformFile) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.sendFile(device, file) }
        }
    }

    fun onSelectDevice(device: BluetoothDevice) {
        selectedDevice = device
    }

    fun sendFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.sendFavorites(selectedDevice!!) }
                .onSuccess { println("Sent favorites") }
                .onFailure { it.printStackTrace() }
        }
    }

    fun sendBlacklisted() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.sendBlacklisted(selectedDevice!!) }
                .onSuccess { println("Sent blacklisted") }
                .onFailure { it.printStackTrace() }
        }
    }

    fun sendLists(uuids: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.sendLists(selectedDevice!!, uuids) }
                .onSuccess { println("Sent lists") }
                .onFailure { it.printStackTrace() }
        }
    }
}