package com.programmersbox.common.di

import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.db.ListRepository
import com.programmersbox.common.presentation.backup.BackupRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun repositoryModule() = module {
    singleOf(::BackupRepository)
    singleOf(::NetworkConnectionRepository)
    singleOf(::ListRepository)
}
