package com.programmersbox.common.di

import com.programmersbox.common.ApplicationInfo
import com.programmersbox.common.BuildKonfig
import com.programmersbox.common.DownloadHandler
import com.programmersbox.common.RestoreWorker
import com.programmersbox.common.getDatabaseBuilder
import com.programmersbox.common.presentation.backup.BackupRestoreHandler
import com.programmersbox.common.presentation.backup.Zipper
import com.programmersbox.common.presentation.components.videoloader.AndroidVideoThumbnailCacheDirectoryProvider
import com.programmersbox.common.presentation.components.videoloader.AndroidVideoThumbnailPlatformExtractor
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailCacheDirectoryProvider
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailPlatformExtractor
import com.programmersbox.common.presentation.qrcode.QrCodeRepository
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun createPlatformModule(): Module = module {
    singleOf(::Zipper)
    singleOf(::QrCodeRepository)
    workerOf(::RestoreWorker)
    singleOf(::DownloadHandler)
    single { getDatabaseBuilder(get()) }
    singleOf(::BackupRestoreHandler)
    factory { ApplicationInfo(BuildKonfig.VERSION_NAME) }

    single<VideoThumbnailCacheDirectoryProvider> { AndroidVideoThumbnailCacheDirectoryProvider(get()) }
    single<VideoThumbnailPlatformExtractor> { AndroidVideoThumbnailPlatformExtractor() }
}