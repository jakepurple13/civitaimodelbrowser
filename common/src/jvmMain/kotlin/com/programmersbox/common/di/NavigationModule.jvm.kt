package com.programmersbox.common.di

import com.programmersbox.common.presentation.settings.JvmSettingsScreen
import org.koin.core.module.Module
import org.koin.dsl.navigation3.navigation

actual fun Module.platformSettingsNavigation() {
    navigation<JvmSettingsScreen> { JvmSettingsScreen() }
}