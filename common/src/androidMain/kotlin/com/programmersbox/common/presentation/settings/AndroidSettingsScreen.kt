package com.programmersbox.common.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.programmersbox.common.BackButton
import com.programmersbox.common.presentation.components.ToastType
import com.programmersbox.common.presentation.components.ToasterState
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
data object AndroidSettingsScreen : NavKey

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AndroidSettingsScreen() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Android Settings") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        val context = LocalContext.current
        val snackBarState = koinInject<ToasterState>()

        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                ListItem(
                    content = { Text("Clear Cache") },
                    leadingContent = { Icon(Icons.Default.Cached, null) },
                    onClick = {
                        runCatching {
                            context
                                .cacheDir
                                .listFiles()
                                ?.forEach {
                                    if (it.name == "journal" || it.name.startsWith("journal.")) {
                                        println("Skipping ${it.name}")
                                        return@forEach
                                    }
                                    println("Deleting ${it.name}")
                                    if (it.deleteRecursively()) {
                                        println("Deleted ${it.name}")
                                    } else {
                                        println("Failed to delete ${it.name}")
                                    }
                                }
                        }
                            .onSuccess {
                                snackBarState.show(
                                    "Cache Cleared",
                                    type = ToastType.Success
                                )
                            }
                            .onFailure {
                                snackBarState.show(
                                    "Failed to Clear Cache",
                                    type = ToastType.Error
                                )
                            }
                            .onFailure { it.printStackTrace() }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )
            }
        }
    }
}