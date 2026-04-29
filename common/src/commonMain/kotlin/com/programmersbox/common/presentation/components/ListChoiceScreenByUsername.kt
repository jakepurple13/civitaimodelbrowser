package com.programmersbox.common.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmersbox.common.CloseButton
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.ListRepository
import com.programmersbox.resources.Res
import com.programmersbox.resources.cancel
import com.programmersbox.resources.choose_a_list
import com.programmersbox.resources.confirm
import com.programmersbox.resources.create_new_list
import com.programmersbox.resources.list_name
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListChoiceScreen(
    username: String,
    onAdd: (List<CustomList>) -> Unit,
    navigationIcon: @Composable () -> Unit = { CloseButton() },
) {
    val listRepository = koinInject<ListRepository>()
    val scope = rememberCoroutineScope()
    val list by listRepository
        .getAllLists()
        .collectAsStateWithLifecycle(emptyList())

    var selectedLists by remember { mutableStateOf<Set<CustomList>>(emptySet()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.choose_a_list)) },
                navigationIcon = navigationIcon,
                actions = {
                    if (list.isNotEmpty()) {
                        Text("(${selectedLists.size}/${list.size})")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedLists.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onAdd(selectedLists.toList()) }
                ) {
                    Icon(Icons.Default.Check, null)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .navigationBarsPadding()
        ) {
            var showAdd by remember { mutableStateOf(false) }
            ElevatedCard(
                onClick = { showAdd = !showAdd },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(Res.string.create_new_list),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    trailingContent = { Icon(Icons.Default.Add, null) }
                )
            }
            if (showAdd) {
                var name by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showAdd = false },
                    title = { Text(stringResource(Res.string.create_new_list)) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(stringResource(Res.string.list_name)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description (optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    listRepository.createList(
                                        name = name,
                                        description = description.takeIf { it.isNotBlank() },
                                        showToast = false
                                    )
                                    showAdd = false
                                }
                            },
                            enabled = name.isNotEmpty()
                        ) { Text(stringResource(Res.string.confirm)) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showAdd = false }
                        ) { Text(stringResource(Res.string.cancel)) }
                    }
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(list, key = { it.item.uuid }) { item ->
                    val isSelected = item in selectedLists
                    val isInList = item
                        .list
                        .find { l -> l.name == username } != null
                    ListItem(
                        enabled = !isInList,
                        leadingContent = {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                enabled = !isInList,
                            )
                        },
                        content = { Text(item.item.name) },
                        trailingContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("(${item.list.size})")
                                if (isInList) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        },
                        checked = isSelected,
                        onCheckedChange = {
                            selectedLists = if (it) {
                                selectedLists + item
                            } else {
                                selectedLists - item
                            }
                        },
                    )
                }
            }
        }
    }
}
