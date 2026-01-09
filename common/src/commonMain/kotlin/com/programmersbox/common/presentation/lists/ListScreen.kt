package com.programmersbox.common.presentation.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.DataStore
import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.toImageHash
import com.programmersbox.common.presentation.components.CivitBottomBar
import com.programmersbox.common.presentation.components.LoadingImage
import com.programmersbox.common.presentation.components.rememberBiometricOpening
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: ListViewModel = koinViewModel(),
) {
    val connectionRepository = koinInject<NetworkConnectionRepository>()
    val shouldShowMedia by remember { derivedStateOf { connectionRepository.shouldShowMedia } }
    val dataTimeFormatter = remember {
        DateTimeFormatHandle(createDateTimeFormatItem(true))
    }
    val dataStore = koinInject<DataStore>()
    val showBlur by dataStore.rememberShowBlur()
    val useProgressive by dataStore.rememberUseProgressive()
    val showNsfw by dataStore.showNsfw()
    val blurStrength by dataStore.hideNsfwStrength()

    val scope = rememberCoroutineScope()
    val hazeState = rememberHazeState(showBlur)
    val hazeStyle = LocalHazeStyle.current

    val bottomBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Lists") },
                navigationIcon = { BackButton() },
                actions = {
                    Text("(${viewModel.list.size})")

                    var showAdd by remember { mutableStateOf(false) }
                    if (showAdd) {
                        var name by remember { mutableStateOf("") }
                        AlertDialog(
                            onDismissRequest = { showAdd = false },
                            title = { Text("Create New List") },
                            text = {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("List Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            viewModel.listDao.create(name)
                                            showAdd = false
                                        }
                                    },
                                    enabled = name.isNotEmpty()
                                ) { Text("Confirm") }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showAdd = false }
                                ) { Text("Cancel") }
                            }
                        )
                    }

                    IconButton(
                        onClick = { showAdd = !showAdd }
                    ) { Icon(Icons.Default.Add, null) }
                },
                colors = if (showBlur) TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                else TopAppBarDefaults.topAppBarColors(),
                modifier = Modifier.hazeEffect(hazeState, hazeStyle) {
                    progressive = if (useProgressive)
                        HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                            preferPerformance = true
                        )
                    else
                        null
                }
            )
        },
        bottomBar = {
            CivitBottomBar(
                showBlur = showBlur,
                bottomBarScrollBehavior = bottomBarScrollBehavior,
                modifier = Modifier.hazeEffect(hazeState) {
                    progressive = if (useProgressive)
                        HazeProgressive.verticalGradient(
                            startIntensity = 0f,
                            endIntensity = 1f,
                            preferPerformance = true
                        )
                    else
                        null
                    style = hazeStyle
                }
            )
        },
        modifier = Modifier.nestedScroll(bottomBarScrollBehavior.nestedScrollConnection)
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .hazeSource(state = hazeState)
        ) {
            item(
                contentType = "search"
            ) {
                Column {
                    var showSearchType by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = viewModel.search,
                        onValueChange = { viewModel.search = it },
                        label = { Text("Search Lists") },
                        singleLine = true,
                        placeholder = { Text("Search Lists") },
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.search = "" }
                            ) { Icon(Icons.Default.Clear, null) }
                        },
                        leadingIcon = {
                            IconButton(
                                onClick = { showSearchType = !showSearchType }
                            ) { Icon(Icons.AutoMirrored.Filled.ManageSearch, null) }
                        },
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    AnimatedVisibility(showSearchType) {
                        Column {
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                SearchType.entries.forEachIndexed { index, searchType ->
                                    SegmentedButton(
                                        selected = viewModel.searchType == searchType,
                                        onClick = { viewModel.searchType = searchType },
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = SearchType.entries.size
                                        ),
                                        label = { Text(searchType.name) }
                                    )
                                }
                            }
                            Text(
                                "DFS: Searches the list name first, then the items",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Text(
                                "BFS: Searches the items first, then the list name",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
            items(
                items = viewModel.searchList,
                contentType = { "list" },
                key = { it.item.uuid }
            ) { list ->
                val biometricOpen = rememberBiometricOpening(
                    title = "Authenticate to view ${list.item.name}",
                    onAuthenticationSucceeded = { onNavigateToDetail(list.item.uuid) }
                )
                ElevatedCard(
                    onClick = {
                        if (list.item.useBiometric) {
                            biometricOpen.authenticate(customListItem = list.item)
                        } else {
                            onNavigateToDetail(list.item.uuid)
                        }
                    },
                    modifier = Modifier.animateItem()
                ) {
                    ListCard(
                        list = list,
                        dateTimeFormatter = dataTimeFormatter,
                        showNsfw = showNsfw,
                        blurStrength = blurStrength.dp,
                        shouldShowMedia = shouldShowMedia
                    )
                }
            }
        }
    }
}

@Composable
private fun ListCard(
    list: CustomList,
    dateTimeFormatter: DateTimeFormatHandle,
    showNsfw: Boolean,
    blurStrength: Dp,
    shouldShowMedia: Boolean,
    modifier: Modifier = Modifier,
) {
    val imageHashing = list.toImageHash()
    val time = remember { dateTimeFormatter.format(list.item.time.toLocalDateTime()) }
    ListItem(
        overlineContent = { Text("Last Updated: $time") },
        trailingContent = { Text("(${list.list.size})") },
        headlineContent = { Text(list.item.name) },
        leadingContent = {
            Box {
                val imageModifier = Modifier
                    .size(ComposableUtils.IMAGE_WIDTH / 3, ComposableUtils.IMAGE_HEIGHT / 3)
                    .clip(MaterialTheme.shapes.medium)

                if (imageHashing?.url?.endsWith("mp4") == true && shouldShowMedia) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(Color.Black)
                            .then(imageModifier)
                    ) {
                        VideoPreviewComposable(
                            url = imageHashing.url,
                            frameCount = 5,
                            contentScale = ContentScale.Crop,
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.matchParentSize()
                        ) {
                            Text("Click to Play")
                            Icon(Icons.Default.PlayArrow, null)
                        }
                    }
                } else {
                    LoadingImage(
                        imageUrl = imageHashing?.url.orEmpty(),
                        isNsfw = list.list.any { it.nsfw },
                        name = list.item.name,
                        hash = imageHashing?.hash,
                        modifier = imageModifier.then(
                            if (!showNsfw && list.list.any { it.nsfw }) {
                                Modifier.blur(blurStrength)
                            } else {
                                Modifier
                            }
                        ),
                    )
                }
                if (list.item.useBiometric) {
                    Icon(
                        Icons.Default.Lock,
                        null,
                        tint = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        },
        supportingContent = {
            Column {
                list.list.take(3).forEach { info ->
                    Text(info.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        },
        modifier = modifier
    )
}

val DateFormatItem = LocalDate.Format {
    monthNumber()
    char('/')
    day(padding = Padding.ZERO)
    char('/')
    year()
}

private val Format24 = LocalTime.Format {
    hour()
    char(':')
    minute()
}

@Stable
data class DateTimeFormatHandle(
    val dateTimeFormatter: DateTimeFormat<LocalDateTime>
) {
    fun format(time: LocalDateTime) = dateTimeFormatter.format(time)
}

private val Format12 = LocalTime.Format {
    amPmHour()
    char(':')
    minute()
    char(' ')
    amPmMarker("AM", "PM")
}

internal fun createDateTimeFormatItem(isUsing24HourTime: Boolean) =
    LocalDateTime.Format {
        date(DateFormatItem)
        chars(", ")
        time(if (isUsing24HourTime) Format24 else Format12)
    }

@OptIn(ExperimentalTime::class)
fun Long.toLocalDateTime() = Instant
    .fromEpochMilliseconds(this)
    .toLocalDateTime(TimeZone.currentSystemDefault())