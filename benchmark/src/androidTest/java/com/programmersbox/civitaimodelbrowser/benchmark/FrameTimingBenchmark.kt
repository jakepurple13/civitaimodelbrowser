package com.programmersbox.civitaimodelbrowser.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class FrameTimingBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollHomeFeed() {
        benchmarkRule.measureRepeated(
            packageName = TargetPackage,
            metrics = listOf(FrameTimingMetric()),
            iterations = 5,
            startupMode = null,
            setupBlock = {
                prepareHomeScreen()
            }
        ) {
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.85f).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.25f).toInt(),
                20
            )
            device.waitForIdle()
        }
    }
}