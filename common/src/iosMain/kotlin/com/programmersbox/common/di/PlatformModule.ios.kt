package com.programmersbox.common.di

import com.programmersbox.common.DownloadHandler
import com.programmersbox.common.NotificationHandler
import com.programmersbox.common.presentation.backup.BackupRestoreHandler
import com.programmersbox.common.presentation.components.videoloader.IosVideoThumbnailCacheDirectoryProvider
import com.programmersbox.common.presentation.components.videoloader.IosVideoThumbnailPlatformExtractor
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailCacheDirectoryProvider
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailPlatformExtractor
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun createPlatformModule(): Module = module {
    singleOf(::DownloadHandler)
    singleOf(::BackupRestoreHandler)
    singleOf(::NotificationHandler)

    single<VideoThumbnailCacheDirectoryProvider> { IosVideoThumbnailCacheDirectoryProvider() }
    single<VideoThumbnailPlatformExtractor> { IosVideoThumbnailPlatformExtractor() }
}