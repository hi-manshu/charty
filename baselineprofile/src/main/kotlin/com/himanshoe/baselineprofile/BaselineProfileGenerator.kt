package com.himanshoe.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TARGET_PACKAGE = "com.himanshoe.sample"
private const val SCROLL_PASSES = 4

/**
 * Records a baseline profile for the sample app by launching it and scrolling the chart gallery, so
 * the hot classes/methods behind Charty's charts get AOT-compiled at install time for consumers.
 * Generate with `./gradlew :composeApp:generateBaselineProfile` against a connected device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    /**
     * Exercises the sample app's startup path and records the profile the AAR ships with, so a
     * consuming app gets Charty's hot paths compiled ahead of time instead of on first render.
     */
    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = TARGET_PACKAGE,
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()
            val list = device.findObject(By.scrollable(true))
            if (list == null) {
                device.waitForIdle()
                return@collect
            }
            repeat(SCROLL_PASSES) {
                list.fling(Direction.DOWN)
                device.waitForIdle()
            }
            repeat(SCROLL_PASSES) {
                list.fling(Direction.UP)
                device.waitForIdle()
            }
        }
    }
}
