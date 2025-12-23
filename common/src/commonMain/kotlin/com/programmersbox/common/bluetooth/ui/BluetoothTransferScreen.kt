package com.programmersbox.common.bluetooth.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmersbox.common.BackButton
import com.programmersbox.common.bluetooth.IncomingFile
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothTransferScreen(
    viewModel: BluetoothTransferViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val pairedDevices by viewModel.pairedDevices.collectAsStateWithLifecycle(emptyList())

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bluetooth Transfer") },
                navigationIcon = { BackButton() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            viewModel
                .incomingFile
                ?.let { file -> IncomingFileCard(file) }

            Card(
                onClick = { viewModel.sendFavorites() },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text("Send Favorites") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null
                        )
                    }
                )
            }

            Card(
                onClick = { viewModel.sendBlacklisted() },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text("Send Blacklisted") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                )
            }

            Text(
                "Paired Devices",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(pairedDevices) { device ->
                    ListItem(
                        headlineContent = { Text(device.name) },
                        supportingContent = { Text(device.address) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Bluetooth,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            if (viewModel.selectedDevice == device) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            viewModel.onSelectDevice(device)
                            /*launcher.launch()*/
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun IncomingFileCard(
    file: IncomingFile,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { /* Handle file */ },
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Incoming File", style = MaterialTheme.typography.titleMedium)
            Text("Name: ${file.name}")
            Text("Size: ${file.content.size} bytes")
            // Here you could add a button to save the file
            Text("Content: ${file.content.decodeToString()}", maxLines = 1)
        }
    }
}
