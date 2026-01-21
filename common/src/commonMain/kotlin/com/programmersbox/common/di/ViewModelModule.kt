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
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun viewModelModule() = module {
    viewModelOf(::CivitAiViewModel)
    viewModelOf(::CivitAiSearchViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::QrCodeScannerViewModel)
    viewModelOf(::CivitAiImagesViewModel)
    viewModelOf(::ListViewModel)
    viewModelOf(::BackupViewModel)
    viewModelOf(::RestoreViewModel)
    viewModelOf(::ListDetailViewModel)
    viewModelOf(::CivitAiDetailViewModel)
    viewModelOf(::CivitAiUserViewModel)
    viewModelOf(::CivitAiModelImagesViewModel)
}