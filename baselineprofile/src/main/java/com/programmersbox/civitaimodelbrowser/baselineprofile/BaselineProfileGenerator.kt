package com.programmersbox.civitaimodelbrowser.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the baseline profile and startup profile for the app.
 *
 * Run with `./gradlew :android:generateReleaseBaselineProfile` on a connected device.
 * Output is written to `android/src/release/generated/baselineProfiles/`.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(
        packageName = TargetPackage,
        includeInStartupProfile = true,
    ) {
        startAndReachHome()
    }

    @Test
    fun homeFeedJourney() = rule.collect(packageName = TargetPackage) {
        startAndReachHome()
        scrollHomeFeed()
        openFirstModelDetail()
    }
}
