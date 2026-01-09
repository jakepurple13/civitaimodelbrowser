package com.programmersbox.common.presentation.blacklisted

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmersbox.common.BackButton
import com.programmersbox.common.db.FavoritesDao
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BlacklistedScreen() {
    val db = koinInject<FavoritesDao>()
    val blacklistedItems by db.getBlacklisted().collectAsStateWithLifecycle(emptyList())
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blacklisted") },
                navigationIcon = { BackButton() },
                actions = { Text(blacklistedItems.size.toString()) },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        LazyColumn(
            contentPadding = padding
        ) {
            items(
                blacklistedItems,
                contentType = { "blacklisted" },
                key = { it.id }
            ) { blacklistedItem ->
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
                                        db.delete(blacklistedItem)
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
                    modifier = Modifier.animateItem()
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