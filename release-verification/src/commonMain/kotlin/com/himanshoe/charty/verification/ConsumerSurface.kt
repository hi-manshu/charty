package com.himanshoe.charty.verification

import androidx.compose.runtime.Composable
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.annotation.ChartyExperimental
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty3d.bar.Bar3DChart
import com.himanshoe.charty3d.bar.config.Bar3DChartConfig
import com.himanshoe.charty3d.pie.Pie3DChart
import com.himanshoe.charty3d.pie.config.Pie3DChartConfig
import com.himanshoe.charty3d.pie.config.Pie3DLabelContent

/**
 * A consumer, written the way a consumer writes one: against published coordinates, with no access
 * to the source.
 *
 * Compiling this is the check. Publishing succeeds long before an artifact is usable — a dependency
 * missing from the POM, a platform variant that never got built, an `api` that should have been
 * `implementation` and so hides a type the caller needs, a version pairing that resolves to two
 * incompatible halves. None of that shows up in the publish task's output. All of it shows up here,
 * because this file cannot compile unless the artifacts are genuinely consumable.
 *
 * It is deliberately unremarkable code. The point is not to exercise every chart; it is to touch the
 * types a real caller cannot avoid — the charts, their configs, the data classes, the colour model,
 * and the opt-in marker — across every published target.
 */
@Composable
private fun FlatCharts() {
    val bars = listOf(BarData(label = "Q1", value = 120f), BarData(label = "Q2", value = 180f))

    BarChart(
        data = { bars },
        color = ChartyColor.Solid(ChartyColors.Blue),
        barConfig = BarChartConfig(animation = Animation.Disabled),
    )

    LineChart(
        data = { listOf(LineData(label = "Mon", value = 20f), LineData(label = "Tue", value = 45f)) },
        color = ChartyColor.Gradient(listOf(ChartyColors.Blue, ChartyColors.Green)),
        lineConfig = LineChartConfig(animation = Animation.Disabled),
    )
}

/**
 * The 3D charts, reached the way the annotation forces a consumer to reach them.
 *
 * The `@OptIn` is the assertion. `@ChartyExperimental` is a `RequiresOptIn` marker at ERROR level, so
 * if it ever stopped being published, stopped being applied to these declarations, or arrived with
 * the wrong retention, this file would either fail to compile or start warning that the opt-in is
 * unnecessary. Either way the mistake surfaces before a release rather than after one.
 */
@OptIn(ChartyExperimental::class)
@Composable
private fun ProjectedCharts() {
    Bar3DChart(
        data = { listOf(BarData(label = "Q1", value = 120f), BarData(label = "Q2", value = 180f)) },
        color = ChartyColor.Solid(ChartyColors.Blue),
        barConfig = Bar3DChartConfig(animation = Animation.Disabled, showValueLabels = true),
        onBarClick = { bar -> println(bar.label) },
    )

    Pie3DChart(
        data = { listOf(PieData(label = "A", value = 40f), PieData(label = "B", value = 60f)) },
        colors = listOf(ChartyColor.Solid(ChartyColors.Blue), ChartyColor.Solid(ChartyColors.Green)),
        pieConfig =
            Pie3DChartConfig(
                animation = Animation.Disabled,
                labelContent = Pie3DLabelContent.PERCENTAGE,
            ),
        onSliceClick = { slice -> println(slice.label) },
    )
}
