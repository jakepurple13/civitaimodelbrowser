package com.programmersbox.common.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import com.programmersbox.common.DownloadHandler
import com.programmersbox.common.LocalActions
import com.programmersbox.common.SheetDetails
import com.programmersbox.common.details.blurGradient
import com.programmersbox.common.rememberSROState
import com.programmersbox.common.scaleRotateOffsetReset
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import org.koin.compose.koinInject

@Composable
fun ImageSheet(
    url: String,
    isNsfw: Boolean,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onRemoveFromFavorite: () -> Unit,
    onDismiss: () -> Unit,
    nsfwText: String,
    actions: @Composable RowScope.() -> Unit = {},
    moreInfo: @Composable () -> Unit = {},
) {
    SheetDetails(
        onDismiss = onDismiss,
        content = {
            if (url.endsWith("mp4")) {
                VideoSheetContent(
                    video = url,
                    isNsfw = isNsfw,
                    isFavorite = isFavorite,
                    onFavorite = onFavorite,
                    onRemoveFromFavorite = onRemoveFromFavorite,
                    nsfwText = nsfwText,
                    actions = actions,
                    moreInfo = moreInfo,
                )
            } else {
                SheetContent(
                    image = url,
                    isNsfw = isNsfw,
                    isFavorite = isFavorite,
                    onFavorite = onFavorite,
                    onRemoveFromFavorite = onRemoveFromFavorite,
                    nsfwText = nsfwText,
                    moreInfo = moreInfo,
                    actions = actions,
                )
            }
        }
    )
}

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
private fun SheetContent(
    image: String,
    isNsfw: Boolean,
    nsfwText: String,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onRemoveFromFavorite: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    moreInfo: @Composable () -> Unit = {},
) {
    val painter = asyncPainterResource(image)
    val downloadHandler = koinInject<DownloadHandler>()
    val scope = rememberCoroutineScope()
    val actions = LocalActions.current
    SelectionContainer(
        modifier = Modifier.navigationBarsPadding()
    ) {
        var showInfo by remember { mutableStateOf(false) }
        if (showInfo) {
            AlertDialog(
                onDismissRequest = { showInfo = false },
                title = { Text("Info") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        if (isNsfw) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                ElevatedAssistChip(
                                    label = { Text("NSFW") },
                                    onClick = {},
                                    colors = AssistChipDefaults.elevatedAssistChipColors(
                                        disabledLabelColor = MaterialTheme.colorScheme.error,
                                        disabledContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    enabled = false,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.error,
                                    ),
                                )

                                ElevatedAssistChip(
                                    label = { Text(nsfwText) },
                                    onClick = {},
                                    colors = AssistChipDefaults.elevatedAssistChipColors(
                                        disabledLabelColor = MaterialTheme.colorScheme.error,
                                        disabledContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    enabled = false,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.error,
                                    ),
                                )
                            }
                        }
                        moreInfo()
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showInfo = false }
                    ) { Text("Done") }
                }
            )
        }

        val sroState = rememberSROState()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    windowInsets = WindowInsets(0.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        //containerColor = BottomSheetDefaults.ContainerColor
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = { showInfo = true }
                        ) { Icon(Icons.Default.Info, null) }
                    },
                    actions = {
                        actions()
                        IconButton(
                            onClick = {
                                if (isFavorite) {
                                    onRemoveFromFavorite()
                                } else {
                                    onFavorite()
                                }
                            }
                        ) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                null
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = Color.Transparent
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { sroState.reset() },
                        icon = { Icon(Icons.Default.Refresh, null) },
                        label = { Text("Reset") },
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { actions.shareUrl(image) },
                        icon = { Icon(Icons.Default.Share, null) },
                        label = { Text("Share") },
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            downloadHandler.download(
                                url = image,
                                name = image.toPath().name
                            )
                        },
                        icon = { Icon(Icons.Default.Download, null) },
                        label = { Text("Download") },
                    )
                }
            },
            containerColor = BottomSheetDefaults.ContainerColor
        ) { padding ->
            val blur = 70.dp
            val alpha = .5f
            val saturation = 3f
            val scaleX = 1.5f
            val scaleY = 1.5f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .scaleRotateOffsetReset(sroState)
            ) {
                KamelImage(
                    resource = { painter },
                    contentDescription = null,
                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                        setToSaturation(
                            saturation
                        )
                    }),
                    modifier = Modifier.blurGradient(blur, alpha, scaleX, scaleY)
                )

                KamelImage(
                    resource = { painter },
                    contentDescription = null,
                    onLoading = {
                        val progress = animateFloatAsState(
                            targetValue = it,
                            label = ""
                        ).value
                        CircularWavyProgressIndicator(
                            progress = { progress }
                        )
                    },
                )
            }
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
private fun VideoSheetContent(
    video: String,
    isNsfw: Boolean,
    nsfwText: String,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onRemoveFromFavorite: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    moreInfo: @Composable () -> Unit = {},
) {
    val downloadHandler = koinInject<DownloadHandler>()
    val scope = rememberCoroutineScope()
    val actions = LocalActions.current
    SelectionContainer(
        modifier = Modifier.navigationBarsPadding()
    ) {
        var showInfo by remember { mutableStateOf(false) }
        if (showInfo) {
            AlertDialog(
                onDismissRequest = { showInfo = false },
                title = { Text("Info") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        if (isNsfw) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                ElevatedAssistChip(
                                    label = { Text("NSFW") },
                                    onClick = {},
                                    colors = AssistChipDefaults.elevatedAssistChipColors(
                                        disabledLabelColor = MaterialTheme.colorScheme.error,
                                        disabledContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    enabled = false,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.error,
                                    ),
                                )

                                ElevatedAssistChip(
                                    label = { Text(nsfwText) },
                                    onClick = {},
                                    colors = AssistChipDefaults.elevatedAssistChipColors(
                                        disabledLabelColor = MaterialTheme.colorScheme.error,
                                        disabledContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    enabled = false,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.error,
                                    ),
                                )
                            }
                        }
                        moreInfo()
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showInfo = false }
                    ) { Text("Done") }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    windowInsets = WindowInsets(0.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        //containerColor = BottomSheetDefaults.ContainerColor
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = { showInfo = true }
                        ) { Icon(Icons.Default.Info, null) }
                    },
                    actions = {
                        actions()
                        IconButton(
                            onClick = {
                                if (isFavorite) {
                                    onRemoveFromFavorite()
                                } else {
                                    onFavorite()
                                }
                            }
                        ) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                null
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = Color.Transparent
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { actions.shareUrl(video) },
                        icon = { Icon(Icons.Default.Share, null) },
                        label = { Text("Share") },
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            downloadHandler.download(
                                url = video,
                                name = video.toPath().name
                            )
                        },
                        icon = { Icon(Icons.Default.Download, null) },
                        label = { Text("Download") },
                    )
                }
            },
            containerColor = BottomSheetDefaults.ContainerColor
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val playerHost = remember {
                    MediaPlayerHost(
                        mediaUrl = video
                    )
                }

                VideoPlayerComposable(
                    playerHost = playerHost,
                    playerConfig = VideoPlayerConfig(
                        isGestureVolumeControlEnabled = false
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun MultipleImageSheet(
    urls: List<String>,
    onDismiss: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    moreInfo: @Composable () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    SheetDetails(
        onDismiss = onDismiss,
        content = {
            val state = rememberPagerState { urls.size }
            Column {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(state.pageCount) { iteration ->
                        val color = if (state.currentPage == iteration)
                            Color.DarkGray
                        else
                            Color.LightGray
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(16.dp)
                                .clickable {
                                    scope.launch {
                                        state.animateScrollToPage(
                                            iteration
                                        )
                                    }
                                }
                        )
                    }
                }
                HorizontalPager(
                    state = state
                ) { page ->
                    val image = urls[page]

                    if (image.endsWith("mp4")) {
                        VideoSheetContent(
                            video = image,
                            isNsfw = false,
                            isFavorite = false,
                            onFavorite = {},
                            onRemoveFromFavorite = {},
                            nsfwText = "",
                            actions = actions,
                            moreInfo = moreInfo,
                        )
                    } else {
                        SheetContent(
                            image = image,
                            isNsfw = false,
                            isFavorite = false,
                            onFavorite = {},
                            onRemoveFromFavorite = {},
                            nsfwText = "",
                            moreInfo = moreInfo,
                            actions = actions,
                        )
                    }
                }
            }
        }
    )
}