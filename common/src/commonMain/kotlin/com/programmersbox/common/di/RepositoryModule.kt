package com.programmersbox.common.di

import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.backup.BackupRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun repositoryModule() = module {
    singleOf(::BackupRepository)
    singleOf(::NetworkConnectionRepository)
}
