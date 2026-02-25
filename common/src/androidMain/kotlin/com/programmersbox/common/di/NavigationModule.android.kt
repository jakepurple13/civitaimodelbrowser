package com.programmersbox.common.di

import com.programmersbox.common.presentation.settings.AndroidSettingsScreen
import org.koin.core.module.Module

actual fun Module.platformSettingsNavigation() {
    settingsNavigation<AndroidSettingsScreen> {
        AndroidSettingsScreen()
    }
}