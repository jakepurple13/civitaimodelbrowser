package com.programmersbox.common.di

import com.programmersbox.common.presentation.settings.JvmSettingsScreen
import org.koin.core.module.Module

actual fun Module.platformSettingsNavigation() {
    settingsNavigation<JvmSettingsScreen> { JvmSettingsScreen() }
}