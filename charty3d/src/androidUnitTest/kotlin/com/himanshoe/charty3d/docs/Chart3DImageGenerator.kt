package com.himanshoe.charty3d.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty3d.bar.Bar3DChart
import com.himanshoe.charty3d.bar.config.Bar3DChartConfig
import com.himanshoe.charty3d.pie.Pie3DChart
import com.himanshoe.charty3d.pie.config.Pie3DChartConfig
import com.himanshoe.charty3d.pie.config.Pie3DLabelContent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders one still of each `charty-3d` chart into `docs/charty/img/`, so its pages open with an
 * image like every other chart's does.
 *
 * A documentation-asset generator, not a regression test: it never asserts and always overwrites.
 * Regenerate with:
 * `./gradlew :charty3d:testDebugUnitTest --tests "com.himanshoe.charty3d.docs.Chart3DImageGenerator" -Proborazzi.test.record=true`
 *
 * Both charts render with [Animation.Disabled], so the capture shows the settled scene rather than a
 * frame of the entrance.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [DOC_SDK], qualifiers = DOC_QUALIFIERS)
class Chart3DImageGenerator {
    @Test
    fun bar3DChart() =
        capture(name = "bar3_d_chart") {
            Bar3DChart(
                data = { quarters },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(Indigo),
                barConfig = Bar3DChartConfig(animation = Animation.Disabled, showValueLabels = true),
            )
        }

    @Test
    fun pie3DChart() =
        capture(name = "pie3_d_chart") {
            Pie3DChart(
                data = { channels },
                modifier = Modifier.fillMaxSize(),
                colors = palette,
                pieConfig =
                    Pie3DChartConfig(
                        animation = Animation.Disabled,
                        labelContent = Pie3DLabelContent.PERCENTAGE,
                    ),
            )
        }

    private fun capture(
        name: String,
        width: Dp = WIDE,
        height: Dp = TALL,
        content: @Composable () -> Unit,
    ) {
        captureRoboImage(filePath = "$DOC_IMAGE_DIR/$name.png") {
            Box(
                modifier =
                    Modifier
                        .size(width = width, height = height)
                        .background(color = Color.White)
                        .padding(all = 16.dp),
            ) {
                content()
            }
        }
    }
}

private const val DOC_SDK = 34
private const val DOC_QUALIFIERS = "w800dp-h500dp-xhdpi"
private const val DOC_IMAGE_DIR = "../docs/charty/img"
private val WIDE = 800.dp
private val TALL = 500.dp

private val Indigo = Color(0xFF6650A4)

private val palette =
    listOf(
        ChartyColor.Solid(Color(0xFF2962FF)),
        ChartyColor.Solid(Color(0xFFE91E63)),
        ChartyColor.Solid(Color(0xFF00BFA5)),
        ChartyColor.Solid(Color(0xFFFF6D00)),
    )

private val quarters =
    listOf(
        BarData(label = "Q1", value = 120f),
        BarData(label = "Q2", value = 180f),
        BarData(label = "Q3", value = 90f),
        BarData(label = "Q4", value = 210f),
    )

private val channels =
    listOf(
        PieData(label = "Direct", value = 40f),
        PieData(label = "Search", value = 30f),
        PieData(label = "Social", value = 20f),
        PieData(label = "Email", value = 10f),
    )
