package com.programmersbox.common

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.programmersbox.common.backup.BackupRestoreHandler
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual class DownloadHandler(
    context: Context,
) {
    private val downloadManager = context.getSystemService<DownloadManager>()
    actual fun download(url: String, name: String) {
        val uri = url.toUri()
        val request = DownloadManager.Request(uri)
            .setTitle(name) // Title shown in the notification
            .setDescription("Downloading $name") // Description in the notification
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Show notification during and after download
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE) // Allow download over Wi-Fi and mobile data
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                name
            ) // Save to public Downloads directory

        downloadManager?.enqueue(request)
    }
}

actual fun createPlatformModule(): Module = module {
    singleOf(::DownloadHandler)
    single { getDatabaseBuilder(get()) }
    singleOf(::BackupRestoreHandler)
}
