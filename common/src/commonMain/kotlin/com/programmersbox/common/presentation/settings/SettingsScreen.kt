package com.programmersbox.common.presentation.settings

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
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
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                onClick = onNavigateToQrCode
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.scan_qr_code)) },
                    leadingContent = { Icon(Icons.Default.QrCodeScanner, null) }
                )
            }

            HorizontalDivider()

            Card(
                onClick = onNavigateToNsfw
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.nsfw_settings)) },
                    leadingContent = { Icon(Icons.Default.NoAdultContent, null) }
                )
            }

            HorizontalDivider()

            Card(
                onClick = onNavigateToBehavior
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.behavior_settings)) },
                    leadingContent = { Icon(Icons.Default.Api, null) }
                )
            }

            HorizontalDivider()

            BackupRestoreSettings(
                onNavigateToBackup = onNavigateToBackup,
                onNavigateToRestore = onNavigateToRestore,
            )

            ExtraSettings()

            HorizontalDivider()

            Card(
                onClick = onNavigateToStats
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.stats)) },
                    leadingContent = { Icon(Icons.Default.QueryStats, null) }
                )
            }

            Card(
                onClick = onNavigateToOnboarding
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.view_onboarding_again)) },
                    leadingContent = { Icon(Icons.Default.CatchingPokemon, null) }
                )
            }

            HorizontalDivider()

            AboutSettings(
                onNavigateToAbout = onNavigateToAbout,
            )
        }
    }
}

@Composable
private fun BackupRestoreSettings(
    onNavigateToBackup: () -> Unit,
    onNavigateToRestore: () -> Unit,
) {
    Column {
        Card(
            onClick = onNavigateToBackup
        ) {
            ListItem(
                headlineContent = { Text(stringResource(Res.string.backup)) },
                leadingContent = { Icon(Icons.Default.Backup, null) }
            )
        }

        Card(
            onClick = onNavigateToRestore
        ) {
            ListItem(
                headlineContent = { Text(stringResource(Res.string.restore)) },
                leadingContent = { Icon(Icons.Default.Restore, null) }
            )
        }
    }
}

@Composable
private fun ColumnScope.AboutSettings(
    onNavigateToAbout: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Card(
        onClick = onNavigateToAbout
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.about)) },
            leadingContent = { Icon(Icons.Default.Info, null) }
        )
    }

    Card(
        onClick = { uriHandler.openUri(Consts.CIVIT_URL) }
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.open_civitai)) },
            leadingContent = {
                Image(
                    painter = painterResource(Res.drawable.civitai_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            }
        )
    }

    Card(
        onClick = { uriHandler.openUri(Consts.CIVIT_REST_API) }
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.open_civitai_rest_api)) },
            leadingContent = {
                Icon(
                    Icons.Github,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            }
        )
    }

    Card(
        onClick = { uriHandler.openUri(Consts.CIVIT_GITHUB) }
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.open_github)) },
            leadingContent = {
                Icon(
                    Icons.Github,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            }
        )
    }

    HorizontalDivider()

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

@Composable
expect fun ColumnScope.ExtraSettings()