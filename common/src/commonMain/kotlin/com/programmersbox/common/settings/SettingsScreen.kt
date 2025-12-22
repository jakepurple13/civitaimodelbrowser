package com.programmersbox.common.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Backup
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
import androidx.compose.material3.Scaffold
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
import com.programmersbox.common.components.CivitBottomBar
import com.programmersbox.common.components.icons.Github
import com.programmersbox.common.getPlatformName
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import org.jetbrains.compose.resources.painterResource
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
    onNavigateToCustomList: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            CivitBottomBar(
                onNavigateToLists = onNavigateToCustomList,
                onNavigateToSettings = {},
                onNavigateToHome = onNavigateToHome,
                isHome = false,
                isSettings = true,
                isLists = false,
                showBlur = false,
                bottomBarScrollBehavior = null,
            )
        },
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
                    headlineContent = { Text("Scan QR Code") },
                    leadingContent = { Icon(Icons.Default.QrCodeScanner, null) }
                )
            }

            HorizontalDivider()

            Card(
                onClick = onNavigateToNsfw
            ) {
                ListItem(
                    headlineContent = { Text("NSFW Settings") },
                    leadingContent = { Icon(Icons.Default.NoAdultContent, null) }
                )
            }

            HorizontalDivider()

            Card(
                onClick = onNavigateToBehavior
            ) {
                ListItem(
                    headlineContent = { Text("Behavior Settings") },
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
                    headlineContent = { Text("Stats") },
                    leadingContent = { Icon(Icons.Default.QueryStats, null) }
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
                headlineContent = { Text("Backup") },
                leadingContent = { Icon(Icons.Default.Backup, null) }
            )
        }

        Card(
            onClick = onNavigateToRestore
        ) {
            ListItem(
                headlineContent = { Text("Restore") },
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
            headlineContent = { Text("About") },
            leadingContent = { Icon(Icons.Default.Info, null) }
        )
    }

    Card(
        onClick = { uriHandler.openUri("https://civitai.com/") }
    ) {
        ListItem(
            headlineContent = { Text("Open CivitAi") },
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
        onClick = { uriHandler.openUri("https://github.com/civitai/civitai/wiki/REST-API-Reference") }
    ) {
        ListItem(
            headlineContent = { Text("Open CivitAi REST API") },
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
        onClick = { uriHandler.openUri("https://github.com/jakepurple13/civitaimodelbrowser/") }
    ) {
        ListItem(
            headlineContent = { Text("Open CivitAi Model Browser GitHub") },
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