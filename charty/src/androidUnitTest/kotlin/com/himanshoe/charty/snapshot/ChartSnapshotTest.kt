package com.himanshoe.charty.snapshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        captureRoboImage(filePath = "src/androidUnitTest/snapshots/$name.png") {
            Box(modifier = Modifier.size(width = 360.dp, height = 220.dp).background(Color.White)) {
                content()
            }
        }
    }
}

private const val SNAPSHOT_SDK = 34
