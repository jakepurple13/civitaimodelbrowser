package com.programmersbox.common.di

import com.programmersbox.common.presentation.backup.BackupViewModel
import com.programmersbox.common.presentation.backup.RestoreViewModel
import com.programmersbox.common.presentation.creator.CivitAiUserViewModel
import com.programmersbox.common.presentation.details.CivitAiDetailViewModel
import com.programmersbox.common.presentation.details.CivitAiModelImagesViewModel
import com.programmersbox.common.presentation.favorites.FavoritesViewModel
import com.programmersbox.common.presentation.home.CivitAiSearchViewModel
import com.programmersbox.common.presentation.home.CivitAiViewModel
import com.programmersbox.common.presentation.images.CivitAiImagesViewModel
import com.programmersbox.common.presentation.lists.ListDetailViewModel
import com.programmersbox.common.presentation.lists.ListViewModel
import com.programmersbox.common.presentation.qrcode.QrCodeScannerViewModel
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
    viewModelOf(::RestoreViewModel)
}