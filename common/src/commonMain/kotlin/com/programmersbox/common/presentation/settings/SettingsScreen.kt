package com.programmersbox.common.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoAdultContent
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.programmersbox.common.ApplicationInfo
import com.programmersbox.common.BackButton
import com.programmersbox.common.Consts
import com.programmersbox.common.WindowedScaffold
import com.programmersbox.common.getPlatformName
import com.programmersbox.common.presentation.components.CivitBottomBar
import com.programmersbox.common.presentation.components.CivitRail
import com.programmersbox.common.presentation.components.icons.Github
import com.programmersbox.resources.Res
import com.programmersbox.resources.about
import com.programmersbox.resources.backup
import com.programmersbox.resources.behavior_settings
import com.programmersbox.resources.civitai_logo
import com.programmersbox.resources.nsfw_settings
import com.programmersbox.resources.open_civitai
import com.programmersbox.resources.open_civitai_rest_api
import com.programmersbox.resources.open_github
import com.programmersbox.resources.restore
import com.programmersbox.resources.scan_qr_code
import com.programmersbox.resources.settings
import com.programmersbox.resources.stats
import com.programmersbox.resources.view_onboarding_again
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToQrCode: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToRestore: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToNsfw: () -> Unit,
    onNavigateToBehavior: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    WindowedScaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(Res.string.settings)) },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            CivitBottomBar(
                showBlur = false,
                bottomBarScrollBehavior = null,
            )
        },
        rail = { CivitRail() },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(padding)
        ) {
            NormalSettings(
                onNavigateToQrCode = onNavigateToQrCode,
                onNavigateToNsfw = onNavigateToNsfw,
                onNavigateToBehavior = onNavigateToBehavior,
            )

            BackupRestoreSettings(
                onNavigateToBackup = onNavigateToBackup,
                onNavigateToRestore = onNavigateToRestore,
            )

            ExtraSettings()

            OtherSettings(
                onNavigateToStats = onNavigateToStats,
                onNavigateToOnboarding = onNavigateToOnboarding,
            )

            AboutSettings(
                onNavigateToAbout = onNavigateToAbout,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NormalSettings(
    onNavigateToQrCode: () -> Unit,
    onNavigateToNsfw: () -> Unit,
    onNavigateToBehavior: () -> Unit,
) {
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        SegmentedListItem(
            content = { Text(stringResource(Res.string.scan_qr_code)) },
            leadingContent = { Icon(Icons.Default.QrCodeScanner, null) },
            onClick = onNavigateToQrCode,
            shapes = ListItemDefaults.segmentedShapes(0, 3),
            colors = colors
        )

        SegmentedListItem(
            content = { Text(stringResource(Res.string.nsfw_settings)) },
            leadingContent = { Icon(Icons.Default.NoAdultContent, null) },
            onClick = onNavigateToNsfw,
            shapes = ListItemDefaults.segmentedShapes(1, 3),
            colors = colors
        )

        SegmentedListItem(
            content = { Text(stringResource(Res.string.behavior_settings)) },
            leadingContent = { Icon(Icons.Default.Api, null) },
            onClick = onNavigateToBehavior,
            shapes = ListItemDefaults.segmentedShapes(2, 3),
            colors = colors
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OtherSettings(
    onNavigateToStats: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
) {
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        SegmentedListItem(
            content = { Text(stringResource(Res.string.stats)) },
            leadingContent = { Icon(Icons.Default.QueryStats, null) },
            onClick = onNavigateToStats,
            shapes = ListItemDefaults.segmentedShapes(0, 2),
            colors = colors
        )

        SegmentedListItem(
            content = { Text(stringResource(Res.string.view_onboarding_again)) },
            leadingContent = { Icon(Icons.Default.CatchingPokemon, null) },
            onClick = onNavigateToOnboarding,
            shapes = ListItemDefaults.segmentedShapes(1, 2),
            colors = colors
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BackupRestoreSettings(
    onNavigateToBackup: () -> Unit,
    onNavigateToRestore: () -> Unit,
) {
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        SegmentedListItem(
            content = { Text(stringResource(Res.string.backup)) },
            leadingContent = { Icon(Icons.Default.Backup, null) },
            onClick = onNavigateToBackup,
            shapes = ListItemDefaults.segmentedShapes(0, 2),
            colors = colors
        )

        SegmentedListItem(
            content = { Text(stringResource(Res.string.restore)) },
            leadingContent = { Icon(Icons.Default.Restore, null) },
            onClick = onNavigateToRestore,
            shapes = ListItemDefaults.segmentedShapes(1, 2),
            colors = colors
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AboutSettings(
    onNavigateToAbout: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        SegmentedListItem(
            content = { Text(stringResource(Res.string.about)) },
            leadingContent = { Icon(Icons.Default.Info, null) },
            onClick = onNavigateToAbout,
            shapes = ListItemDefaults.segmentedShapes(0, 4),
            colors = colors
        )

        SegmentedListItem(
            content = { Text(stringResource(Res.string.open_civitai)) },
            leadingContent = {
                Image(
                    painter = painterResource(Res.drawable.civitai_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            },
            onClick = { uriHandler.openUri(Consts.CIVIT_URL) },
            shapes = ListItemDefaults.segmentedShapes(1, 4),
            colors = colors
        )

        SegmentedListItem(
            content = { Text(stringResource(Res.string.open_civitai_rest_api)) },
            leadingContent = {
                Icon(
                    Icons.Github,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            },
            onClick = { uriHandler.openUri(Consts.CIVIT_REST_API) },
            shapes = ListItemDefaults.segmentedShapes(2, 4),
            colors = colors
        )

        SegmentedListItem(
            content = { Text(stringResource(Res.string.open_github)) },
            leadingContent = {
                Icon(
                    Icons.Github,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            },
            onClick = { uriHandler.openUri(Consts.CIVIT_GITHUB) },
            shapes = ListItemDefaults.segmentedShapes(3, 4),
            colors = colors
        )

        Spacer(Modifier.padding(6.dp))

        Text(
            remember { getPlatformName() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            koinInject<ApplicationInfo>().versionName,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
expect fun ColumnScope.ExtraSettings()