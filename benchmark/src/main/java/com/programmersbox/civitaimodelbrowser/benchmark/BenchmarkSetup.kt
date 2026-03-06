package com.programmersbox.civitaimodelbrowser.benchmark

import android.content.Intent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal const val TargetPackage = "com.programmersbox.civitaimodelbrowser"

private const val TargetActivity = "com.programmersbox.civitaimodelbrowser.MainActivity"
private const val UiTimeoutMs = 5_000L

internal fun MacrobenchmarkScope.prepareHomeScreen() {
    pressHome()
    startTargetActivityAndWait()
    device.dismissPermissionDialogIfPresent()
    device.dismissOnboardingIfPresent()
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.openSettingsSection(sectionLabel: String) {
    device.clickByLabel("Settings")
    device.waitForObject("Settings")
    device.clickByLabel(sectionLabel)
    device.waitForObject(sectionLabel)
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.startTargetActivityAndWait() {
    startActivityAndWait(
        Intent(Intent.ACTION_MAIN).apply {
            setClassName(TargetPackage, TargetActivity)
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    )
}

private fun UiDevice.dismissPermissionDialogIfPresent() {
    listOf(
        "com.android.permissioncontroller" to "permission_allow_button",
        "com.android.permissioncontroller" to "permission_allow_foreground_only_button",
        "com.android.packageinstaller" to "permission_allow_button"
    ).firstNotNullOfOrNull { (pkg, res) ->
        wait(Until.findObject(By.res(pkg, res)), 500)
    }?.click()
}

private fun UiDevice.dismissOnboardingIfPresent() {
    val skipButton = waitForObject("Skip", timeoutMs = UiTimeoutMs) ?: return
    skipButton.click()
    waitForObject("Confirm", timeoutMs = UiTimeoutMs)?.click()
}

private fun UiDevice.clickByLabel(label: String, timeoutMs: Long = UiTimeoutMs) {
    waitForObject(label, timeoutMs)?.click()
        ?: error("Unable to find UI element labeled '$label'")
}

private fun UiDevice.waitForObject(label: String, timeoutMs: Long = UiTimeoutMs): UiObject2? {
    val selectors = listOf(
        By.text(label),
        By.desc(label),
        By.textContains(label),
        By.descContains(label),
    )

    return selectors.firstNotNullOfOrNull { selector ->
        wait(Until.findObject(selector), timeoutMs)
    }
}