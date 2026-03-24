package com.programmersbox.common.di

import com.programmersbox.common.ApplicationInfo
import com.programmersbox.common.BuildKonfig
import com.programmersbox.common.DownloadHandler
import com.programmersbox.common.NotificationHandler
import com.programmersbox.common.getDatabaseBuilder
import com.programmersbox.common.presentation.backup.BackupRestoreHandler
import com.programmersbox.common.presentation.backup.Zipper
import com.programmersbox.common.presentation.components.videoloader.IosVideoThumbnailCacheDirectoryProvider
import com.programmersbox.common.presentation.components.videoloader.IosVideoThumbnailPlatformExtractor
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailCacheDirectoryProvider
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailPlatformExtractor
import com.programmersbox.common.presentation.qrcode.QrCodeRepository
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun createPlatformModule(): Module = module {
    singleOf(::DownloadHandler)
    singleOf(::BackupRestoreHandler)
    singleOf(::NotificationHandler)

    single<VideoThumbnailCacheDirectoryProvider> { IosVideoThumbnailCacheDirectoryProvider() }
    single<VideoThumbnailPlatformExtractor> { IosVideoThumbnailPlatformExtractor() }

    factory { ApplicationInfo(BuildKonfig.VERSION_NAME) }
    single<() -> String> {
        {
            val documentDirectory: NSURL? = NSFileManager
                .defaultManager
                .URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
            requireNotNull(documentDirectory).path + "/androidx.preferences_pb"
        }
    }
    single { getDatabaseBuilder() }
    singleOf(::QrCodeRepository)
    singleOf(::Zipper)
}