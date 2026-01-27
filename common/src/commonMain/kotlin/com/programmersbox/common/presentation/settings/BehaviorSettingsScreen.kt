package com.programmersbox.common.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.BlurLinear
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.BorderBottom
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
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
import com.programmersbox.common.DoubleClickBehavior
import com.programmersbox.common.HazeBlur
import com.programmersbox.common.ThemeMode
import com.programmersbox.resources.Res
import com.programmersbox.resources.behavior_settings
import com.programmersbox.resources.blur_type
import com.programmersbox.resources.civitai_logo
import com.programmersbox.resources.confirm
import com.programmersbox.resources.double_click_behavior
import com.programmersbox.resources.progressive_blur_description
import com.programmersbox.resources.show_blur
import com.programmersbox.resources.theme_mode
import com.programmersbox.resources.use_amoled_mode
import com.programmersbox.resources.use_progressive_blur
import com.programmersbox.resources.use_toolbar
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
fun BehaviorSettingsScreen() {
    val dataStore = koinInject<DataStore>()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(Res.string.behavior_settings)) },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        BehaviorSettings(
            dataStore = dataStore,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BehaviorSettings(
    dataStore: DataStore,
    modifier: Modifier = Modifier,
) {
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        var useToolbar by dataStore.rememberUseToolbar()
        var showBlur by dataStore.rememberShowBlur()
        var blurType by dataStore.rememberBlurType()
        var useProgressive by dataStore.rememberUseProgressive()
        var isAmoled by dataStore.rememberIsAmoled()
        var doubleClickBehavior by dataStore.rememberDoubleClickBehavior()
        var showFavorites by dataStore.rememberShowFavorites()

        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            SegmentedListItem(
                leadingContent = {
                    Icon(
                        if (showBlur) Icons.Default.BlurOn else Icons.Default.BlurOff,
                        null
                    )
                },
                content = { Text(stringResource(Res.string.show_blur)) },
                trailingContent = {
                    Switch(
                        checked = showBlur,
                        onCheckedChange = null
                    )
                },
                onClick = { showBlur = !showBlur },
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(
                    0,
                    if (showBlur) 3 else 1
                )
            )

            AnimatedVisibility(showBlur) {
                var showBlurOptions by remember { mutableStateOf(false) }

                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    SegmentedListItem(
                        leadingContent = {
                            Icon(
                                if (useProgressive)
                                    Icons.Default.BlurCircular
                                else
                                    Icons.Default.BlurLinear,
                                null
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
                        onClick = { useProgressive = !useProgressive },
                        colors = colors,
                        shapes = ListItemDefaults.segmentedShapes(1, 3)
                    )

                    SegmentedListItem(
                        content = { Text(stringResource(Res.string.blur_type)) },
                        trailingContent = { Text("${blurType.type.name} ${blurType.level.name}") },
                        leadingContent = { Icon(Icons.Default.BlurCircular, null) },
                        onClick = { showBlurOptions = !showBlurOptions },
                        colors = colors,
                        shapes = ListItemDefaults.segmentedShapes(2, 3)
                    )

                    AnimatedVisibility(showBlurOptions) {
                        Column {
                            HazeBlur.entries.forEachIndexed { index, blur ->
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    blur.levels.forEach { level ->
                                        Surface(
                                            onClick = { blurType = BlurType(blur, level) },
                                            shape = MaterialTheme.shapes.large
                                        ) {
                                            Box {
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
                                                    style = blur.toHazeStyle(level)
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
                                }
                                if (index < HazeBlur.entries.size - 1) HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            SegmentedListItem(
                leadingContent = { Icon(Icons.Default.BorderBottom, null) },
                content = { Text(stringResource(Res.string.use_toolbar)) },
                trailingContent = { Switch(checked = useToolbar, onCheckedChange = null) },
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(0, 3),
                onClick = { useToolbar = !useToolbar }
            )

            var themeMode by dataStore.rememberThemeMode()

            var showThemeModeDialog by remember { mutableStateOf(false) }

            if (showThemeModeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeModeDialog = false },
                    title = { Text(stringResource(Res.string.theme_mode)) },
                    text = {
                        Column {
                            ThemeMode.entries.forEach {
                                Card(
                                    onClick = { themeMode = it },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    ListItem(
                                        headlineContent = { Text(it.name) },
                                        trailingContent = {
                                            RadioButton(
                                                selected = it == themeMode,
                                                onClick = null
                                            )
                                        },
                                        colors = ListItemDefaults.colors(
                                            containerColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showThemeModeDialog = false }
                        ) { Text(stringResource(Res.string.confirm)) }
                    }
                )
            }

            SegmentedListItem(
                leadingContent = { Icon(Icons.Default.Brightness4, null) },
                content = { Text(stringResource(Res.string.theme_mode)) },
                supportingContent = { Text(themeMode.name) },
                trailingContent = { Icon(Icons.Default.ArrowDropDown, null) },
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(1, 3),
                onClick = { showThemeModeDialog = true }
            )

            SegmentedListItem(
                leadingContent = { Icon(Icons.Default.Brightness7, null) },
                content = { Text(stringResource(Res.string.use_amoled_mode)) },
                trailingContent = { Switch(checked = isAmoled, onCheckedChange = null) },
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(2, 3),
                onClick = { isAmoled = !isAmoled }
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            SegmentedListItem(
                leadingContent = { Icon(Icons.Default.Favorite, null) },
                content = { Text("Show Favorites") },
                supportingContent = { Text("Show favorites in the list") },
                trailingContent = {
                    Switch(
                        checked = showFavorites,
                        onCheckedChange = null
                    )
                },
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(0, 2),
                onClick = { showFavorites = !showFavorites }
            )

            var showDoubleClickBehaviorDialog by remember { mutableStateOf(false) }

            if (showDoubleClickBehaviorDialog) {
                AlertDialog(
                    onDismissRequest = { showDoubleClickBehaviorDialog = false },
                    title = { Text(stringResource(Res.string.double_click_behavior)) },
                    text = {
                        Column {
                            DoubleClickBehavior.entries.forEach {
                                Card(
                                    onClick = { doubleClickBehavior = it },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    ListItem(
                                        headlineContent = { Text(it.visualName) },
                                        trailingContent = {
                                            RadioButton(
                                                selected = it == doubleClickBehavior,
                                                onClick = null
                                            )
                                        },
                                        colors = ListItemDefaults.colors(
                                            containerColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showDoubleClickBehaviorDialog = false }
                        ) { Text(stringResource(Res.string.confirm)) }
                    }
                )
            }

            SegmentedListItem(
                leadingContent = { Icon(Icons.Default.AdsClick, null) },
                content = { Text(stringResource(Res.string.double_click_behavior)) },
                supportingContent = { Text(doubleClickBehavior.visualName) },
                trailingContent = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        null
                    )
                },
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(1, 2),
                onClick = { showDoubleClickBehaviorDialog = true }
            )
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
                .hazeEffect(state = state) {
                    this.style = style
                }
                .padding(16.dp),
        ) {
            Text(name)
        }
    }
}