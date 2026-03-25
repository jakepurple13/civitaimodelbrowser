package com.programmersbox.common.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.SettingsSystemDaydream
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.from
import com.materialkolor.palettes.TonalPalette
import com.materialkolor.rememberDynamicColorScheme
import com.programmersbox.common.BackButton
import com.programmersbox.common.DataStore
import com.programmersbox.common.ThemeColor
import com.programmersbox.common.ThemeMode
import com.programmersbox.common.presentation.components.DiagonalWipeIcon
import com.programmersbox.common.presentation.components.DiagonalWipeIconDefaults
import com.programmersbox.resources.Res
import com.programmersbox.resources.confirm
import com.programmersbox.resources.theme_mode
import com.programmersbox.resources.use_amoled_mode
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Theme Settings") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        ThemeSettings(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettings(
    modifier: Modifier = Modifier,
    dataStore: DataStore = koinInject(),
) {
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    var isAmoled by dataStore.rememberIsAmoled()

    var themeColor by dataStore.rememberThemeColor()

    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
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
                leadingContent = {
                    Icon(
                        when (themeMode) {
                            ThemeMode.System -> Icons.Default.SettingsSystemDaydream
                            ThemeMode.Light -> Icons.Default.Brightness4
                            ThemeMode.Dark -> Icons.Default.DarkMode
                        },
                        null
                    )
                },
                content = { Text(stringResource(Res.string.theme_mode)) },
                supportingContent = { Text(themeMode.name) },
                trailingContent = { Icon(Icons.Default.ArrowDropDown, null) },
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(0, 3),
                onClick = { showThemeModeDialog = true }
            )

            SegmentedListItem(
                leadingContent = {
                    DiagonalWipeIcon(
                        isWiped = isAmoled,
                        wipedIcon = Icons.Default.Brightness7,
                        baseIcon = Icons.Default.Brightness4,
                        motion = DiagonalWipeIconDefaults.expressive(),
                        modifier = Modifier.size(24.dp)
                    )
                },
                content = { Text(stringResource(Res.string.use_amoled_mode)) },
                trailingContent = { Switch(checked = isAmoled, onCheckedChange = null) },
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(1, 3),
                checked = isAmoled,
                onCheckedChange = { isAmoled = it },
            )

            Card(
                shape = ListItemDefaults.segmentedShapes(2, 3).shape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                ListItem(
                    headlineContent = { Text("Select Theme Color") },
                    leadingContent = { Icon(Icons.Default.ColorLens, null) },
                    colors = colors,
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        4.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    ThemeColor
                        .entries
                        .forEach {
                            ThemeItem(
                                onClick = { themeColor = it },
                                selected = themeColor == it,
                                themeColor = it,
                                colorScheme = if (it == ThemeColor.Dynamic)
                                    MaterialTheme.colorScheme
                                else
                                    rememberDynamicColorScheme(
                                        it.seedColor,
                                        isAmoled = isAmoled,
                                        isDark = isSystemInDarkTheme()
                                    )
                            )
                        }
                }
            }
        }
    }
}

@Composable
fun ThemeItem(
    onClick: () -> Unit,
    selected: Boolean,
    themeColor: ThemeColor,
    colorScheme: ColorScheme,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            SelectableMiniPalette(
                selected = selected,
                colorScheme = colorScheme
            )

            Text(themeColor.name)
        }
    }
}

@Composable
fun SelectableMiniPalette(
    selected: Boolean,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    SelectableMiniPalette(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        accents = remember(colorScheme) {
            persistentListOf(
                TonalPalette.from(colorScheme.primary),
                TonalPalette.from(colorScheme.secondary),
                TonalPalette.from(colorScheme.tertiary)
            )
        }
    )
}

@Composable
fun SelectableMiniPalette(
    selected: Boolean,
    accents: PersistentList<TonalPalette>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val size = 50
    val content: @Composable () -> Unit = {
        Box {
            Surface(
                modifier = Modifier
                    .size(size.dp)
                    .offset((-25).dp, 25.dp),
                color = Color(accents[1].tone(85)),
            ) {}
            Surface(
                modifier = Modifier
                    .size(size.dp)
                    .offset(25.dp, 25.dp),
                color = Color(accents[2].tone(75)),
            ) {}
            val animationSpec = spring<Float>(stiffness = Spring.StiffnessMedium)
            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(animationSpec) + fadeIn(animationSpec),
                exit = scaleOut(animationSpec) + fadeOut(animationSpec),
            ) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Checked",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
    onClick?.let {
        Surface(
            onClick = onClick,
            modifier = modifier
                .padding(12.dp)
                .size(size.dp),
            shape = CircleShape,
            color = Color(accents[0].tone(60)),
        ) { content() }
    } ?: Surface(
        modifier = modifier
            .padding(12.dp)
            .size(size.dp),
        shape = CircleShape,
        color = Color(accents[0].tone(60)),
    ) { content() }
}