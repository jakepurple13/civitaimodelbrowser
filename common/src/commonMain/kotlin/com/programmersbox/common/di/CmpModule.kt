package com.programmersbox.common.di

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import com.programmersbox.common.DataStore
import com.programmersbox.common.KtorPluginProvider
import com.programmersbox.common.Network
import com.programmersbox.common.db.AppDatabase
import com.programmersbox.common.db.getRoomDatabase
import com.programmersbox.common.presentation.components.ToasterState
import com.programmersbox.common.presentation.components.videoloader.DefaultVideoThumbnailFrameLoader
import com.programmersbox.common.presentation.components.videoloader.KtorVideoThumbnailUrlResolver
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailDiskCache
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailFrameLoader
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailUrlResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun cmpModules() = module {
    singleOf(::getRoomDatabase)
    singleOf(DataStore::getStore)
    single { Network(*getAll<KtorPluginProvider>().toTypedArray()) }
    single { get<AppDatabase>().getDao() }
    single { get<AppDatabase>().getListDao() }
    single { get<AppDatabase>().getSearchHistoryDao() }
    single { ToasterState(CoroutineScope(Dispatchers.Main)) }

    singleOf(::KtorVideoThumbnailUrlResolver) bind VideoThumbnailUrlResolver::class
    singleOf(::DefaultVideoThumbnailFrameLoader) bind VideoThumbnailFrameLoader::class
    single { VideoThumbnailDiskCache(get()) }

    includes(
        viewModelModule(),
        repositoryModule(),
        navigationModule()
    )
}