package com.programmersbox.common.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.BorderBottom
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SettingsSystemDaydream
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmersbox.common.BackButton
import com.programmersbox.common.DataStore
import com.programmersbox.common.DoubleClickBehavior
import com.programmersbox.common.presentation.components.DiagonalWipeIcon
import com.programmersbox.common.presentation.components.DiagonalWipeIconDefaults
import com.programmersbox.common.presentation.components.WipeDirection
import com.programmersbox.resources.Res
import com.programmersbox.resources.behavior_settings
import com.programmersbox.resources.confirm
import com.programmersbox.resources.double_click_behavior
import com.programmersbox.resources.use_toolbar
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorSettingsScreen(
    onNavigateToBlurSettings: (() -> Unit)?,
    onNavigateToThemeSettings: (() -> Unit)?
) {
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
            onNavigateToBlurSettings = onNavigateToBlurSettings,
            onNavigateToThemeSettings = onNavigateToThemeSettings,
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BehaviorSettings(
    dataStore: DataStore,
    onNavigateToBlurSettings: (() -> Unit)? = null,
    onNavigateToThemeSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        ApiKeySetting(dataStore)

        var useToolbar by dataStore.rememberUseToolbar()
        var doubleClickBehavior by dataStore.rememberDoubleClickBehavior()
        var showFavorites by dataStore.rememberShowFavorites()

        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            onNavigateToBlurSettings?.let {
                SegmentedListItem(
                    content = { Text("Blur Settings") },
                    onClick = it,
                    leadingContent = { Icon(Icons.Default.BlurOn, null) },
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(0, 2),
                )
            }

            onNavigateToThemeSettings?.let {
                SegmentedListItem(
                    content = { Text("Theme Settings") },
                    onClick = it,
                    leadingContent = { Icon(Icons.Default.SettingsSystemDaydream, null) },
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(1, 2),
                )
            }
        }

        SegmentedListItem(
            leadingContent = { Icon(Icons.Default.BorderBottom, null) },
            content = { Text(stringResource(Res.string.use_toolbar)) },
            trailingContent = { Switch(checked = useToolbar, onCheckedChange = null) },
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(0, 1),
            checked = useToolbar,
            onCheckedChange = { useToolbar = it },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            SegmentedListItem(
                leadingContent = {
                    DiagonalWipeIcon(
                        isWiped = showFavorites,
                        wipedIcon = Icons.Default.Favorite,
                        baseIcon = Icons.Default.FavoriteBorder,
                        motion = DiagonalWipeIconDefaults.expressive(),
                        modifier = Modifier.size(24.dp)
                    )
                },
                content = { Text("Show Favorites") },
                supportingContent = { Text("Show favorites in the list") },
                trailingContent = {
                    Switch(
                        checked = showFavorites,
                        onCheckedChange = null
                    )
                },
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(0, 3),
                checked = showFavorites,
                onCheckedChange = { showFavorites = it },
            )

            var showNewCardLook by dataStore.rememberUseNewCardLook()

            SegmentedListItem(
                leadingContent = {
                    Icon(Icons.Default.CreditCard, null)
                    DiagonalWipeIcon(
                        isWiped = showNewCardLook,
                        wipedIcon = Icons.Default.CreditCard,
                        baseIcon = Icons.Default.CreditCardOff,
                        motion = DiagonalWipeIconDefaults.expressive(
                            WipeDirection.BottomRightToTopLeft
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                },
                content = { Text("Use new card look?") },
                supportingContent = { Text("Use the new card look") },
                trailingContent = {
                    Switch(
                        checked = showNewCardLook,
                        onCheckedChange = null
                    )
                },
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(1, 3),
                checked = showNewCardLook,
                onCheckedChange = { showNewCardLook = it },
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
                shapes = ListItemDefaults.segmentedShapes(2, 3),
                onClick = { showDoubleClickBehaviorDialog = true }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ApiKeySetting(
    dataStore: DataStore
) {

    val scope = rememberCoroutineScope()
    val apiTokenHolder = dataStore.apiToken

    val apiToken by apiTokenHolder
        .flow
        .collectAsStateWithLifecycle(null)

    val textFieldState = remember(apiToken) {
        TextFieldState(initialText = apiToken.orEmpty())
    }

    OutlinedSecureTextField(
        state = textFieldState,
        label = { Text("Api Token") },
        supportingText = { Text("Get your key at civitai.com/user/account") },
        leadingIcon = {
            IconButton(
                onClick = { textFieldState.clearText() }
            ) { Icon(Icons.Default.Clear, null) }
        },
        trailingIcon = {
            TextButton(
                onClick = {
                    scope.launch {
                        apiTokenHolder.update(textFieldState.text.toString())
                    }
                }
            ) { Text("Apply") }
        },
        shape = ListItemDefaults.segmentedShapes(0, 1).shape,
        modifier = Modifier.fillMaxWidth()
    )
}