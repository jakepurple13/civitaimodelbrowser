package com.programmersbox.common.di

import com.programmersbox.common.presentation.settings.AndroidSettingsScreen
import com.programmersbox.common.presentation.settings.AndroidSettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

actual fun Module.platformSettingsNavigation() {
    viewModelOf(::AndroidSettingsViewModel)
    settingsNavigation<AndroidSettingsScreen> {
        AndroidSettingsScreen()
    }
}