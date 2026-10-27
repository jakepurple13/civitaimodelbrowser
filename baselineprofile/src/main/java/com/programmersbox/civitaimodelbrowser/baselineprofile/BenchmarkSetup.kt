package com.programmersbox.civitaimodelbrowser.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal const val TargetPackage = "com.programmersbox.civitaimodelbrowser"

private const val UiTimeoutMs = 5_000L
private const val FeedTimeoutMs = 15_000L

/**
 * Cold-starts the app and gets past first-run UI (notification permission prompt and onboarding)
 * so the home feed is showing.
 */
internal fun MacrobenchmarkScope.startAndReachHome() {
    pressHome()
    startActivityAndWait()
    device.dismissPermissionDialogIfPresent()
    device.dismissOnboardingIfPresent()
    device.waitForIdle()
}

/** Waits for the home feed grid and flings it down and back up. */
internal fun MacrobenchmarkScope.scrollHomeFeed() {
    device.waitForFeed() ?: error("Home feed never appeared")
    // Wait for the first page of models so there is something to scroll
    device.wait(Until.hasObject(By.pkg(TargetPackage).scrollable(true).hasChild(By.clickable(true))), FeedTimeoutMs)
    repeat(2) { device.flingFeed(Direction.DOWN) }
    device.flingFeed(Direction.UP)
}

/** Opens the first visible model in the home feed, then navigates back. */
internal fun MacrobenchmarkScope.openFirstModelDetail() {
    val item = device.waitForFeed()
        ?.findObject(By.clickable(true))
        ?: return
    runCatching { item.click() }
    device.waitForIdle(UiTimeoutMs)
    device.pressBack()
    device.waitForIdle()
}

/**
 * Paging and pull-to-refresh can recreate the grid's node mid-gesture, so look it up fresh for
 * every fling and retry once if it goes stale.
 */
private fun UiDevice.flingFeed(direction: Direction) {
    repeat(2) {
        val feed = waitForFeed() ?: return
        feed.setGestureMargin(displayWidth / 5)
        val flung = runCatching { feed.fling(direction) }.isSuccess
        waitForIdle()
        if (flung) return
    }
}

private fun UiDevice.waitForFeed(): UiObject2? =
    wait(Until.findObject(By.pkg(TargetPackage).scrollable(true)), FeedTimeoutMs)

private fun UiDevice.dismissPermissionDialogIfPresent() {
    listOf(
        "com.android.permissioncontroller" to "permission_allow_button",
        "com.android.permissioncontroller" to "permission_deny_button",
        "com.android.packageinstaller" to "permission_allow_button",
    ).firstNotNullOfOrNull { (pkg, res) ->
        wait(Until.findObject(By.res(pkg, res)), 1_000)
    }?.click()
}

private fun UiDevice.dismissOnboardingIfPresent() {
    val skipButton = wait(Until.findObject(By.text("Skip")), UiTimeoutMs) ?: return
    skipButton.click()
    wait(Until.findObject(By.text("Confirm")), UiTimeoutMs)?.click()
    waitForIdle()
}
