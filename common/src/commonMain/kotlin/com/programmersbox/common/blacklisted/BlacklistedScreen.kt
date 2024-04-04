package com.programmersbox.common.blacklisted

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.programmersbox.common.LocalDatabase
import com.programmersbox.common.LocalNavController
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BlacklistedScreen() {
    val navController = LocalNavController.current
    val db = LocalDatabase.current
    val blacklistedItems by db.getBlacklistedItems().collectAsStateWithLifecycle(emptyList())
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blacklisted") },
                navigationIcon = {
                    IconButton(
                        onClick = navController::popBackStack,
                    ) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = { Text(blacklistedItems.size.toString()) },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        LazyColumn(
            contentPadding = padding
        ) {
            items(blacklistedItems) { blacklistedItem ->
                var showDialog by remember { mutableStateOf(false) }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Remove from Blacklist?") },
                        text = {
                            Text("See the model again!")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        db.removeBlacklistItem(blacklistedItem)
                                        showDialog = false
                                    }
                                }
                            ) { Text("Confirm") }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDialog = false }
                            ) { Text("Dismiss") }
                        }
                    )
                }
                OutlinedCard(
                    onClick = { showDialog = true },
                    modifier = Modifier.animateItemPlacement()
                ) {
                    ListItem(
                        overlineContent = { Text(blacklistedItem.id.toString()) },
                        headlineContent = { Text(blacklistedItem.name) },
                        supportingContent = {
                            Text(
                                if (blacklistedItem.imageUrl == null) {
                                    "Model"
                                } else {
                                    "Image"
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}