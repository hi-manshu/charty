@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
    "CyclomaticComplexMethod",
    "ktlint:standard:max-line-length",
)

package com.himanshoe.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.himanshoe.charty.bar.GroupedHorizontalBarChart
import com.himanshoe.charty.bar.HorizontalBarChart
import com.himanshoe.charty.bar.StackedBarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.GroupedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import kotlin.random.Random

private fun animationOf(animate: Boolean): Animation =
    if (animate) {
        Animation.Default
    } else {
        Animation.Disabled
    }

private fun randomBarGroups(
    groups: Int,
    series: Int,
    tick: Int,
): List<BarGroup> {
    val random = Random(seed = tick * 1000 + groups * 10 + series)
    return List(groups) { g -> BarGroup(label = "G${g + 1}", values = List(series) { 20f + random.nextFloat() * 80f }) }
}

private fun groupsCode(data: List<BarGroup>): String =
    data.joinToString(
        ",\n            ",
    ) { g -> "BarGroup(\"${g.label}\", listOf(${g.values.joinToString(", ") { fc(it) }}))" }

@Composable
internal fun HorizontalBarPlayground() {
    var bars by remember { mutableStateOf(6) }
    var tick by remember { mutableStateOf(0) }
    var barWidthFraction by remember { mutableStateOf(0.6f) }
    var corner by remember { mutableStateOf<CornerRadius>(CornerRadius.Medium) }
    var color by remember { mutableStateOf(playgroundPalette[3]) }
    val animate = LocalPlaygroundAnimate.current

    val data = remember(bars, tick) { randomValues(bars, tick).mapIndexed { i, v -> BarData("B${i + 1}", v) } }
    val code =
        """
        val data = listOf(${data.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> BarData("B${'$'}{i + 1}", v) }

        HorizontalBarChart(
            data = { data },
            color = ChartyColor.Solid(Color(${colorHex(color)})),
            barConfig = BarChartConfig(
                barWidthFraction = ${fc(barWidthFraction)},
                cornerRadius = CornerRadius.${cornerName(corner)},
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            HorizontalBarChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                color = ChartyColor.Solid(color),
                barConfig =
                    BarChartConfig(
                        barWidthFraction = barWidthFraction,
                        cornerRadius = corner,
                        animation = animationOf(animate),
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Bars", value = bars, valueRange = 2..15, onValueChange = { bars = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Bars")
            SliderRow(label = "Width fraction", value = barWidthFraction, valueRange = 0.1f..1f, onValueChange = {
                barWidthFraction =
                    it
            }, decimals = 2)
            ChoiceRow(label = "Corner radius", options = cornerOptions, selected = corner, labelOf = ::cornerLabel, onSelect = {
                corner =
                    it
            })
            ControlSection(title = "Color")
            ColorRow(label = "Bar color", selected = color, onSelect = { color = it })
        },
    )
}

@Composable
internal fun StackedBarPlayground() {
    var groups by remember { mutableStateOf(6) }
    var series by remember { mutableStateOf(3) }
    var tick by remember { mutableStateOf(0) }
    var barWidthFraction by remember { mutableStateOf(0.6f) }
    var corner by remember { mutableStateOf<CornerRadius>(CornerRadius.Medium) }
    val animate = LocalPlaygroundAnimate.current

    val data = remember(groups, series, tick) { randomBarGroups(groups, series, tick) }
    val code =
        """
        val data = listOf(
            ${groupsCode(data)},
        )

        StackedBarChart(
            data = { data },
            colors = ChartyColor.Gradient(palette),
            stackedConfig = StackedBarChartConfig(
                barWidthFraction = ${fc(barWidthFraction)},
                topCornerRadius = CornerRadius.${cornerName(corner)},
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            StackedBarChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                colors = ChartyColor.Gradient(playgroundPalette.take(series.coerceAtLeast(2))),
                stackedConfig =
                    StackedBarChartConfig(
                        barWidthFraction = barWidthFraction,
                        topCornerRadius = corner,
                        animation = animationOf(animate),
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Groups", value = groups, valueRange = 2..12, onValueChange = { groups = it })
            IntSliderRow(label = "Series", value = series, valueRange = 2..5, onValueChange = { series = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Bars")
            SliderRow(label = "Width fraction", value = barWidthFraction, valueRange = 0.2f..1f, onValueChange = {
                barWidthFraction =
                    it
            }, decimals = 2)
            ChoiceRow(label = "Top corner", options = cornerOptions, selected = corner, labelOf = ::cornerLabel, onSelect = {
                corner =
                    it
            })
        },
    )
}

@Composable
internal fun GroupedBarPlayground() {
    var groups by remember { mutableStateOf(5) }
    var series by remember { mutableStateOf(3) }
    var tick by remember { mutableStateOf(0) }
    var barWidthFraction by remember { mutableStateOf(0.8f) }
    var corner by remember { mutableStateOf<CornerRadius>(CornerRadius.Medium) }
    val animate = LocalPlaygroundAnimate.current

    val data = remember(groups, series, tick) { randomBarGroups(groups, series, tick) }
    val code =
        """
        val data = listOf(
            ${groupsCode(data)},
        )

        GroupedHorizontalBarChart(
            data = { data },
            colors = ChartyColor.Gradient(palette),
            config = GroupedHorizontalBarChartConfig(
                barWidthFraction = ${fc(barWidthFraction)},
                cornerRadius = CornerRadius.${cornerName(corner)},
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            GroupedHorizontalBarChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                colors = ChartyColor.Gradient(playgroundPalette.take(series.coerceAtLeast(2))),
                config =
                    GroupedHorizontalBarChartConfig(
                        barWidthFraction = barWidthFraction,
                        cornerRadius = corner,
                        animation = animationOf(animate),
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Groups", value = groups, valueRange = 2..8, onValueChange = { groups = it })
            IntSliderRow(label = "Series", value = series, valueRange = 2..4, onValueChange = { series = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Bars")
            SliderRow(label = "Width fraction", value = barWidthFraction, valueRange = 0.3f..1f, onValueChange = {
                barWidthFraction =
                    it
            }, decimals = 2)
            ChoiceRow(label = "Corner radius", options = cornerOptions, selected = corner, labelOf = ::cornerLabel, onSelect = {
                corner =
                    it
            })
        },
    )
}
