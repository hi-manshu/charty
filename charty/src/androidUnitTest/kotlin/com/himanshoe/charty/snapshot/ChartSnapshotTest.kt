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
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.pie.PieChart
import com.himanshoe.charty.pie.config.PieChartConfig
import com.himanshoe.charty.pie.data.PieData
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Golden-image snapshot tests: render each chart with a fixed dataset (animation disabled so the
 * capture is deterministic) and compare against a committed PNG. Regenerate baselines with
 * `./gradlew :charty:recordRoborazziDebug`; verify in CI with `:charty:verifyRoborazziDebug`.
 *
 * Comparison allows a small fraction of pixels to differ. Baselines are recorded on whichever
 * machine a contributor happens to use, and the platforms do not anti-alias identically — a curve or
 * a glyph edge lands on slightly different subpixels on Linux than on macOS. Demanding an exact match
 * makes the suite fail for everyone whose OS differs from whoever recorded last, which teaches people
 * to re-record on red rather than to read the diff. The threshold is well below what any real change
 * to a chart moves: a shifted axis, a wrong colour, or a missing series is orders of magnitude more
 * pixels than edge noise.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SNAPSHOT_SDK])
class ChartSnapshotTest {
    @Test
    fun barChart() =
        capture(name = "bar_chart") {
            BarChart(
                data = { listOf(BarData("A", 20f), BarData("B", 45f), BarData("C", 30f), BarData("D", 55f)) },
                color = ChartyColor.Solid(ChartyColors.Blue),
                barConfig = BarChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun lineChart() =
        capture(name = "line_chart") {
            LineChart(
                data = {
                    listOf(
                        LineData("Mon", 20f),
                        LineData("Tue", 45f),
                        LineData("Wed", 30f),
                        LineData("Thu", 60f),
                    )
                },
                color = ChartyColor.Solid(ChartyColors.Blue),
                lineConfig = LineChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun pieChart() =
        capture(name = "pie_chart") {
            PieChart(
                data = {
                    listOf(
                        PieData("A", 30f, ChartyColor.Solid(ChartyColors.Blue)),
                        PieData("B", 45f, ChartyColor.Solid(ChartyColors.Green)),
                        PieData("C", 25f, ChartyColor.Solid(ChartyColors.Orange)),
                    )
                },
                config = PieChartConfig(animation = Animation.Disabled),
            )
        }

    private fun capture(
        name: String,
        content: @Composable () -> Unit,
    ) {
        captureRoboImage(
            filePath = "src/androidUnitTest/snapshots/$name.png",
            roborazziOptions =
                RoborazziOptions(
                    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = ANTIALIASING_TOLERANCE),
                ),
        ) {
            Box(modifier = Modifier.size(width = 360.dp, height = 220.dp).background(Color.White)) {
                content()
            }
        }
    }
}

private const val SNAPSHOT_SDK = 34
private const val ANTIALIASING_TOLERANCE = 0.01f
