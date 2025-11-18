package com.programmersbox.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

actual class DownloadHandler(
    private val network: Network,
    private val dataStoreHandler: DataStoreHandler,
    private val trayState: TrayState
) {
    actual suspend fun download(url: String, name: String) {
        val parent = File(dataStoreHandler.downloadPath.get())
        val file = File(
            parent,
            name
        )

        if (!parent.exists()) {
            parent.mkdirs()
        }

        if (!file.exists()) {
            file.createNewFile()
        }

        file.writeBytes(network.client.get(url).body())

        trayState.sendNotification(
            Notification(
                title = "Download Complete",
                message = "Downloaded $name",
                type = Notification.Type.Info
            )
        )
    }
}

actual fun createPlatformModule(): Module = module {
    singleOf(::DownloadHandler)
    singleOf(::DataStoreHandler)
}

class DataStoreHandler(
    private val dataStore: DataStore
) {
    val downloadPath = DataStore.DataStoreTypeNonNull(
        key = stringPreferencesKey("download_path"),
        dataStore = dataStore.dataStore,
        defaultValue = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "CivitAi"
    )

    @Composable
    fun rememberDownloadPath() = rememberPreference(
        downloadPath.key,
        downloadPath.defaultValue
    )

    @Composable
    private fun <T> rememberPreference(
        key: Preferences.Key<T>,
        defaultValue: T,
    ): MutableState<T> {
        val coroutineScope = rememberCoroutineScope()
        val state by remember {
            dataStore.dataStore.data.map { it[key] ?: defaultValue }
        }.collectAsStateWithLifecycle(initialValue = defaultValue)

        return remember(state) {
            object : MutableState<T> {
                override var value: T
                    get() = state
                    set(value) {
                        coroutineScope.launch {
                            dataStore.dataStore.edit { it[key] = value }
                        }
                    }

                override fun component1() = value
                override fun component2(): (T) -> Unit = { value = it }
            }
        }
    }
}