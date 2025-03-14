package com.programmersbox.civitaimodelbrowser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.programmersbox.common.LocalDatabaseDao
import com.programmersbox.common.UIShow
import com.programmersbox.common.db.CivitDb
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            CustomMaterialTheme {
                val toaster = rememberToasterState()
                val defaultUriHandler = LocalUriHandler.current
                val customUriHandler = remember { customTabsUriHandler { defaultUriHandler.openUri(it) } }
                CompositionLocalProvider(
                    LocalUriHandler provides customUriHandler
                ) {
                    var listToExport by remember { mutableStateOf(CivitDb(emptyList(), emptyList())) }
                    val exportLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.CreateDocument("application/json")
                    ) { document ->
                        document?.let {
                            lifecycleScope.launch {
                                writeToFile(it, listToExport)
                                listToExport = CivitDb(emptyList(), emptyList())
                                toaster.show(
                                    Toast(
                                        message = "Export Completed",
                                        type = ToastType.Success
                                    )
                                )
                            }
                        }
                    }

                    UIShow(
                        onShareClick = {
                            startActivity(
                                Intent.createChooser(
                                    Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, it)
                                        type = "text/plain"
                                    },
                                    null
                                )
                            )
                        },
                        producePath = { filesDir.resolve("androidx.preferences_pb").absolutePath },
                        onExport = {
                            listToExport = it
                            exportLauncher.launch("civitmodelbrowser.json")
                        },
                        onImport = { "" },
                        import = {
                            val dao = LocalDatabaseDao.current
                            val importLauncher2 = rememberLauncherForActivityResult(
                                ActivityResultContracts.OpenDocument()
                            ) { document ->
                                document?.let { uri ->
                                    contentResolver.openInputStream(uri)
                                        ?.use { inputStream -> BufferedReader(InputStreamReader(inputStream)).readText() }
                                        ?.let {
                                            lifecycleScope.launch {
                                                dao.importFavorites(it)
                                                toaster.show(
                                                    Toast(
                                                        message = "Import Completed",
                                                        type = ToastType.Success
                                                    )
                                                )
                                            }
                                        }
                                }
                            }
                            Card(
                                onClick = {
                                    importLauncher2.launch(arrayOf("application/json"))
                                }
                            ) {
                                ListItem(
                                    headlineContent = { Text("Import Favorites") }
                                )
                            }
                        }
                    )
                }
                Toaster(
                    state = toaster,
                    richColors = true
                )
            }
        }
    }
}

private suspend fun Context.writeToFile(document: Uri, list: CivitDb) {
    val json = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    runCatching {
        runBlocking {
            try {
                contentResolver.openFileDescriptor(document, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { f ->
                        f.write(json.encodeToString(list).toByteArray())
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
        .onSuccess { println("Written!") }
        .onFailure { it.printStackTrace() }
}

fun Context.customTabsUriHandler(
    onFailure: (String) -> Unit,
) = object : UriHandler {
    override fun openUri(uri: String) {
        runCatching {
            CustomTabsIntent.Builder()
                .setExitAnimations(
                    this@customTabsUriHandler,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .build().launchUrl(this@customTabsUriHandler, uri.toUri())
        }.onFailure { onFailure(uri) }
    }
}

@Composable
fun CustomMaterialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            activity.window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
