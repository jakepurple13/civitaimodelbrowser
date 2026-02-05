package com.programmersbox.common.di

import com.programmersbox.common.presentation.settings.AndroidSettingsScreen
import org.koin.core.module.Module
import org.koin.dsl.navigation3.navigation

actual fun Module.platformSettingsNavigation() {
    navigation<AndroidSettingsScreen> {
        AndroidSettingsScreen()
    }
}