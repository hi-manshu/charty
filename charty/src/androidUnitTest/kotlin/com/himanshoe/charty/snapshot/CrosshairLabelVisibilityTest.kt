package com.himanshoe.charty.snapshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val CROSSHAIR_SNAPSHOT_SDK = 34
private const val CROSSHAIR_ANTIALIASING_TOLERANCE = 0.01f

/**
 * Pins that a crosshair's `showLabel` flag decides whether a label appears.
 *
 * The flag had stopped working on every line-family chart. The label used to be drawn on the canvas,
 * where the flag was read; when it became a Composable overlay the check did not come with it. Nothing
 * caught that, because the parameter which had carried the old behaviour was still threaded through
 * five files — hard-coded to `false` at every call site, which made a dead path look like a live one.
 *
 * Only rendering can answer this: the question is whether pixels appear. The `false` golden is a blank
 * frame, so a regression that brings the label back has nowhere to hide.
 *
 * Where the badge lands is incidental — the host is placed here without a chart under it, so it has no
 * plot bounds to position against. This test is about presence, not placement.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [CROSSHAIR_SNAPSHOT_SDK])
class CrosshairLabelVisibilityTest {
    @Test
    fun crosshairLabelShown() = capture(name = "crosshair_label_shown", showLabel = true)

    @Test
    fun crosshairLabelHidden() = capture(name = "crosshair_label_hidden", showLabel = false)

    private fun capture(
        name: String,
        showLabel: Boolean,
    ) {
        captureRoboImage(
            filePath = "src/androidUnitTest/snapshots/$name.png",
            roborazziOptions =
                RoborazziOptions(
                    compareOptions =
                        RoborazziOptions.CompareOptions(changeThreshold = CROSSHAIR_ANTIALIASING_TOLERANCE),
                ),
        ) {
            CrosshairLabelUnderTest(showLabel = showLabel)
        }
    }
}

@Composable
private fun CrosshairLabelUnderTest(showLabel: Boolean) {
    Box(modifier = Modifier.size(width = 360.dp, height = 220.dp).background(color = Color.White)) {
        ChartCrosshairHost(
            crosshair = ChartCrosshair<Float>(config = ChartCrosshairConfig(showLabel = showLabel)),
            item = 42f,
            state = CrosshairState(x = 260f, y = 140f, label = "42"),
        )
    }
}
