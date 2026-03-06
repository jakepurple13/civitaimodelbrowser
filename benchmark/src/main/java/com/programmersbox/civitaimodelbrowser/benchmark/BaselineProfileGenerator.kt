package com.programmersbox.civitaimodelbrowser.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startup() = baselineProfileRule.collect(
        packageName = TargetPackage,
        includeInStartupProfile = true,
    ) {
        prepareHomeScreen()
    }

    @Test
    fun openBehaviorSettings() = baselineProfileRule.collect(
        packageName = TargetPackage,
    ) {
        prepareHomeScreen()
        openSettingsSection("Behavior Settings")
    }
}