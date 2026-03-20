package com.programmersbox.common.di

import com.programmersbox.common.DownloadHandler
import com.programmersbox.common.getDatabaseBuilder
import com.programmersbox.common.presentation.backup.BackupRestoreHandler
import com.programmersbox.common.presentation.components.videoloader.AndroidVideoThumbnailCacheDirectoryProvider
import com.programmersbox.common.presentation.components.videoloader.AndroidVideoThumbnailPlatformExtractor
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailCacheDirectoryProvider
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailPlatformExtractor
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun createPlatformModule(): Module = module {
    singleOf(::DownloadHandler)
    single { getDatabaseBuilder(get()) }
    singleOf(::BackupRestoreHandler)

    single<VideoThumbnailCacheDirectoryProvider> { AndroidVideoThumbnailCacheDirectoryProvider(get()) }
    single<VideoThumbnailPlatformExtractor> { AndroidVideoThumbnailPlatformExtractor() }
}