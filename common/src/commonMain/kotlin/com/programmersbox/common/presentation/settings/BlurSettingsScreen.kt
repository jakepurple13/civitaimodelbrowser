package com.programmersbox.common.presentation.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.BlurLinear
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Deblur
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.programmersbox.common.BackButton
import com.programmersbox.common.BlurType
import com.programmersbox.common.DataStore
import com.programmersbox.common.HazeBlur
import com.programmersbox.common.presentation.components.BlurKind
import com.programmersbox.common.presentation.components.CivitBottomBar
import com.programmersbox.common.presentation.components.DiagonalWipeIcon
import com.programmersbox.common.presentation.components.DiagonalWipeIconDefaults
import com.programmersbox.common.presentation.components.WipeDirection
import com.programmersbox.common.presentation.components.rememberBlurKindState
import com.programmersbox.common.presentation.components.setBlurKind
import com.programmersbox.common.presentation.components.setBlurKindSource
import com.programmersbox.resources.Res
import com.programmersbox.resources.blur_type
import com.programmersbox.resources.cancel
import com.programmersbox.resources.civitai_logo
import com.programmersbox.resources.confirm
import com.programmersbox.resources.progressive_blur_description
import com.programmersbox.resources.show_blur
import com.programmersbox.resources.use_progressive_blur
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlurSettingsScreen() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Blur Settings") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        BlurSettings(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BlurSettings(
    modifier: Modifier = Modifier,
    dataStore: DataStore = koinInject(),
) {
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    var showBlur by dataStore.rememberShowBlur()
    var blurType by dataStore.rememberBlurType()
    var useProgressive by dataStore.rememberUseProgressive()

    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        SegmentedListItem(
            leadingContent = {
                DiagonalWipeIcon(
                    isWiped = showBlur,
                    wipedIcon = Icons.Default.BlurOn,
                    baseIcon = Icons.Default.BlurOff,
                    motion = DiagonalWipeIconDefaults.expressive(
                        WipeDirection.BottomRightToTopLeft
                    ),
                    modifier = Modifier.size(24.dp)
                )
            },
            content = { Text(stringResource(Res.string.show_blur)) },
            trailingContent = {
                Switch(
                    checked = showBlur,
                    onCheckedChange = null
                )
            },
            checked = showBlur,
            onCheckedChange = { showBlur = it },
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(
                0,
                if (showBlur) 3 else 1
            )
        )

        AnimatedVisibility(showBlur) {
            var showBlurOptions by remember { mutableStateOf(false) }

            var blurKind by dataStore.rememberBlurKind()
            var showBlurKindDialog by remember { mutableStateOf(false) }

            if (showBlurKindDialog) {
                AlertDialog(
                    onDismissRequest = { showBlurKindDialog = false },
                    title = { Text("Choose Blur Kind") },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            BlurKind.entries.forEach {
                                ListItem(
                                    content = { Text(it.name) },
                                    onClick = { blurKind = it },
                                    trailingContent = {
                                        RadioButton(
                                            selected = it == blurKind,
                                            onClick = null
                                        )
                                    },
                                    selected = it == blurKind,
                                    shapes = ListItemDefaults.shapes(
                                        shape = MaterialTheme.shapes.large,
                                    )
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showBlurKindDialog = false }
                        ) { Text(stringResource(Res.string.confirm)) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showBlurKindDialog = false }
                        ) { Text(stringResource(Res.string.cancel)) }
                    }
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                SegmentedListItem(
                    leadingContent = { Icon(Icons.Default.Deblur, null) },
                    content = { Text("Blur Kind") },
                    supportingContent = { Text("Choose the kind of blur to use.\nCurrently selected ${blurKind.name}") },
                    trailingContent = { Icon(Icons.Default.ArrowDropDown, null) },
                    colors = colors,
                    onClick = { showBlurKindDialog = true },
                    shapes = ListItemDefaults.segmentedShapes(1, 4)
                )

                val blurKindState = rememberBlurKindState()

                AnimatedContent(
                    targetState = blurKind,
                    transitionSpec = {
                        slideInVertically() togetherWith slideOutVertically()
                    }
                ) { target ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        when (target) {
                            BlurKind.Haze -> {
                                SegmentedListItem(
                                    leadingContent = {
                                        DiagonalWipeIcon(
                                            isWiped = useProgressive,
                                            wipedIcon = Icons.Default.BlurCircular,
                                            baseIcon = Icons.Default.BlurLinear,
                                            motion = DiagonalWipeIconDefaults.expressive(),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    content = { Text(stringResource(Res.string.use_progressive_blur)) },
                                    supportingContent = { Text(stringResource(Res.string.progressive_blur_description)) },
                                    trailingContent = {
                                        Switch(
                                            checked = useProgressive,
                                            onCheckedChange = null
                                        )
                                    },
                                    checked = useProgressive,
                                    onCheckedChange = { useProgressive = it },
                                    colors = colors,
                                    shapes = ListItemDefaults.segmentedShapes(2, 4)
                                )

                                SegmentedListItem(
                                    content = { Text(stringResource(Res.string.blur_type)) },
                                    trailingContent = { Text("${blurType.type.name} ${blurType.level.name}") },
                                    leadingContent = { Icon(Icons.Default.BlurCircular, null) },
                                    checked = showBlurOptions,
                                    onCheckedChange = { showBlurOptions = it },
                                    colors = colors,
                                    shapes = ListItemDefaults.segmentedShapes(3, 4)
                                )

                                AnimatedVisibility(showBlurOptions) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        userScrollEnabled = true,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.height(200.dp)
                                    ) {
                                        HazeBlur.entries.forEachIndexed { index, blur ->
                                            items(
                                                items = blur.levels,
                                                contentType = { _ -> "blurLevel" },
                                                key = { "${blur.name}${it.name}" }
                                            ) { level ->
                                                Surface(
                                                    onClick = {
                                                        blurType = BlurType(blur, level)
                                                    },
                                                    shape = MaterialTheme.shapes.large
                                                ) {
                                                    Box(
                                                        modifier = Modifier.size(128.dp)
                                                    ) {
                                                        val hazeState = rememberHazeState()
                                                        Image(
                                                            painter = painterResource(Res.drawable.civitai_logo),
                                                            contentDescription = null,
                                                            contentScale = ContentScale.FillBounds,
                                                            modifier = Modifier
                                                                .matchParentSize()
                                                                .hazeSource(hazeState)
                                                        )

                                                        MaterialsCard(
                                                            name = "$blur $level",
                                                            state = hazeState,
                                                            shape = MaterialTheme.shapes.large,
                                                            style = blur.toHazeStyle(level),
                                                            modifier = Modifier.matchParentSize()
                                                        )

                                                        if (blurType == BlurType(blur, level)) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                null,
                                                                modifier = Modifier
                                                                    .align(Alignment.Center)
                                                                    .padding(8.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            if (index < HazeBlur.entries.size - 1)
                                                item(
                                                    span = { GridItemSpan(maxLineSpan) },
                                                    contentType = "blurDivider"
                                                ) { HorizontalDivider() }
                                        }
                                    }
                                }
                            }

                            BlurKind.LiquidGlass -> {

                            }
                        }

                        Box {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.setBlurKindSource(blurKindState)
                            ) {
                                repeat(6) {
                                    Image(
                                        painter = painterResource(Res.drawable.civitai_logo),
                                        contentDescription = null,
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                            }

                            CivitBottomBar(
                                showBlur = true,
                                bottomBarScrollBehavior = null,
                                modifier = Modifier.setBlurKind(blurKindState) {
                                    progressive = if (blurKindState.hazeState.useProgressive)
                                        HazeProgressive.verticalGradient(
                                            startIntensity = 0f,
                                            endIntensity = 1f,
                                            preferPerformance = true
                                        )
                                    else
                                        null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialsCard(
    name: String,
    state: HazeState,
    style: HazeStyle,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier.size(160.dp),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .hazeEffect(state = state, style = style)
                .padding(16.dp),
        ) {
            Text(name)
        }
    }
}