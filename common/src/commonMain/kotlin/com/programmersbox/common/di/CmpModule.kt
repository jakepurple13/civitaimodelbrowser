package com.programmersbox.common.di

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import com.programmersbox.common.DataStore
import com.programmersbox.common.Network
import com.programmersbox.common.db.AppDatabase
import com.programmersbox.common.db.getRoomDatabase
import com.programmersbox.common.presentation.components.ToasterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun cmpModules() = module {
    singleOf(::Network)
    singleOf(::getRoomDatabase)
    singleOf(DataStore::getStore)
    single { get<AppDatabase>().getDao() }
    single { get<AppDatabase>().getListDao() }
    single { get<AppDatabase>().getSearchHistoryDao() }
    single { ToasterState(CoroutineScope(Dispatchers.Main)) }

    includes(
        viewModelModule(),
        repositoryModule(),
        navigationModule()
    )
}