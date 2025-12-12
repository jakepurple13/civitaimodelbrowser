package com.programmersbox.common.di

import com.programmersbox.common.backup.BackupViewModel
import com.programmersbox.common.creator.CivitAiUserViewModel
import com.programmersbox.common.db.FavoritesViewModel
import com.programmersbox.common.details.CivitAiDetailViewModel
import com.programmersbox.common.details.CivitAiModelImagesViewModel
import com.programmersbox.common.home.CivitAiSearchViewModel
import com.programmersbox.common.home.CivitAiViewModel
import com.programmersbox.common.images.CivitAiImagesViewModel
import com.programmersbox.common.lists.ListDetailViewModel
import com.programmersbox.common.lists.ListViewModel
import com.programmersbox.common.qrcode.QrCodeScannerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun viewModelModule() = module {
    viewModelOf(::CivitAiViewModel)
    viewModelOf(::CivitAiSearchViewModel)
    viewModel { CivitAiDetailViewModel(get(), it.get(), get()) }
    viewModel {
        CivitAiModelImagesViewModel(
            modelId = it.get(),
            dataStore = get(),
            network = get(),
            database = get()
        )
    }
    viewModel {
        CivitAiUserViewModel(
            network = get(),
            dataStore = get(),
            database = get(),
            username = it.get()
        )
    }
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::QrCodeScannerViewModel)
    viewModelOf(::CivitAiImagesViewModel)
    viewModelOf(::ListViewModel)
    viewModel {
        ListDetailViewModel(
            listDao = get(),
            uuid = it.get()
        )
    }

    viewModelOf(::BackupViewModel)
}