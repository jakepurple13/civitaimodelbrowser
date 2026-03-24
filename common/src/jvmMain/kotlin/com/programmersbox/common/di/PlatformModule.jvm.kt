package com.programmersbox.common.di

import androidx.compose.ui.window.TrayState
import ca.gosyer.appdirs.AppDirs
import com.programmersbox.common.DataStoreHandler
import com.programmersbox.common.DownloadHandler
import com.programmersbox.common.getDatabaseBuilder
import com.programmersbox.common.presentation.backup.BackupRestoreHandler
import com.programmersbox.common.presentation.backup.Zipper
import com.programmersbox.common.presentation.components.videoloader.JvmVideoThumbnailCacheDirectoryProvider
import com.programmersbox.common.presentation.components.videoloader.JvmVideoThumbnailPlatformExtractor
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailCacheDirectoryProvider
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailPlatformExtractor
import com.programmersbox.common.presentation.qrcode.QrCodeRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

actual fun createPlatformModule(): Module = module {
    singleOf(::DownloadHandler)
    singleOf(::DataStoreHandler)
    singleOf(::BackupRestoreHandler)

    single {
        AppDirs {
            appName = "CivitAiModelBrowser"
            appAuthor = "jakepurple13"
        }
    }
    single {
        {
            File(
                get<AppDirs>().getUserDataDir(),
                "androidx.preferences_pb"
            ).absolutePath
        }
    }
    singleOf(::getDatabaseBuilder)
    singleOf(::TrayState)
    singleOf(::QrCodeRepository)
    singleOf(::Zipper)

    single<VideoThumbnailCacheDirectoryProvider> { JvmVideoThumbnailCacheDirectoryProvider(get()) }
    single<VideoThumbnailPlatformExtractor> { JvmVideoThumbnailPlatformExtractor() }
}