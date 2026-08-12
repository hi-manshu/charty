@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
    "CyclomaticComplexMethod",
)

package com.himanshoe.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.combo.ComboChart
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.line.AreaChart
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.config.LineInterpolation
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.pie.PieChart
import com.himanshoe.charty.pie.config.PieChartConfig
import com.himanshoe.charty.pie.config.PieChartStyle
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty.point.BubbleChart
import com.himanshoe.charty.point.PointChart
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.BubbleData
import com.himanshoe.charty.point.data.PointData

internal val cornerOptions =
    listOf(CornerRadius.None, CornerRadius.Small, CornerRadius.Medium, CornerRadius.Large, CornerRadius.ExtraLarge)

internal fun cornerLabel(corner: CornerRadius): String =
    when (corner) {
        CornerRadius.None -> "None"
        CornerRadius.Small -> "S"
        CornerRadius.Medium -> "M"
        CornerRadius.Large -> "L"
        CornerRadius.ExtraLarge -> "XL"
        else -> "Custom"
    }

internal fun cornerName(corner: CornerRadius): String =
    when (corner) {
        CornerRadius.None -> "None"
        CornerRadius.Small -> "Small"
        CornerRadius.Medium -> "Medium"
        CornerRadius.Large -> "Large"
        CornerRadius.ExtraLarge -> "ExtraLarge"
        else -> "Custom"
    }

@Composable
internal fun LinePlayground() {
    var points by remember { mutableStateOf(12) }
    var tick by remember { mutableStateOf(0) }
    var lineWidth by remember { mutableStateOf(3f) }
    var pointRadius by remember { mutableStateOf(6f) }
    var showPoints by remember { mutableStateOf(true) }
    var interpolation by remember { mutableStateOf(LineInterpolation.LINEAR) }
    var color by remember { mutableStateOf(playgroundPalette[0]) }

    val data =
        remember(
            points,
            tick,
        ) { randomValues(points, tick).mapIndexed { i, v -> LineData(label = i.toString(), value = v) } }

    val code =
        """
        val data = listOf(${data.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> LineData(i.toString(), v) }

        LineChart(
            data = { data },
            color = ChartyColor.Solid(Color(${colorHex(color)})),
            lineConfig = LineChartConfig(
                lineWidth = ${fc(lineWidth)},
                showPoints = $showPoints,
                pointRadius = ${fc(pointRadius)},
                interpolation = LineInterpolation.${interpolation.name},
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            LineChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                color = ChartyColor.Solid(color),
                lineConfig =
                    LineChartConfig(
                        lineWidth = lineWidth,
                        showPoints = showPoints,
                        pointRadius = pointRadius,
                        interpolation = interpolation,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Points", value = points, valueRange = 3..60, onValueChange = { points = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Line")
            SliderRow(label = "Line width", value = lineWidth, valueRange = 1f..8f, onValueChange = { lineWidth = it })
            ChoiceRow(label = "Interpolation", options = LineInterpolation.entries.toList(), selected = interpolation, labelOf = {
                it.name
            }, onSelect = {
                interpolation =
                    it
            })
            SwitchRow(label = "Show points", checked = showPoints, onCheckedChange = { showPoints = it })
            if (showPoints) {
                SliderRow(label = "Point radius", value = pointRadius, valueRange = 2f..14f, onValueChange = {
                    pointRadius =
                        it
                })
            }
            ControlSection(title = "Color")
            ColorRow(label = "Line color", selected = color, onSelect = { color = it })
        },
    )
}

@Composable
internal fun AreaPlayground() {
    var points by remember { mutableStateOf(10) }
    var tick by remember { mutableStateOf(0) }
    var lineWidth by remember { mutableStateOf(3f) }
    var fillAlpha by remember { mutableStateOf(0.3f) }
    var showPoints by remember { mutableStateOf(false) }
    var interpolation by remember { mutableStateOf(LineInterpolation.SMOOTH) }
    var color by remember { mutableStateOf(playgroundPalette[2]) }

    val data =
        remember(
            points,
            tick,
        ) { randomValues(points, tick).mapIndexed { i, v -> LineData(label = i.toString(), value = v) } }

    val code =
        """
        val data = listOf(${data.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> LineData(i.toString(), v) }

        AreaChart(
            data = { data },
            color = ChartyColor.Solid(Color(${colorHex(color)})),
            lineConfig = LineChartConfig(
                lineWidth = ${fc(lineWidth)},
                showPoints = $showPoints,
                interpolation = LineInterpolation.${interpolation.name},
                fillAlpha = ${fc(fillAlpha)},
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            AreaChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                color = ChartyColor.Solid(color),
                lineConfig =
                    LineChartConfig(
                        lineWidth = lineWidth,
                        showPoints = showPoints,
                        interpolation = interpolation,
                        fillAlpha = fillAlpha,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Points", value = points, valueRange = 3..60, onValueChange = { points = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Area")
            SliderRow(label = "Line width", value = lineWidth, valueRange = 1f..8f, onValueChange = { lineWidth = it })
            SliderRow(label = "Fill alpha", value = fillAlpha, valueRange = 0f..1f, onValueChange = {
                fillAlpha = it
            }, decimals = 2)
            ChoiceRow(label = "Interpolation", options = LineInterpolation.entries.toList(), selected = interpolation, labelOf = {
                it.name
            }, onSelect = {
                interpolation =
                    it
            })
            SwitchRow(label = "Show points", checked = showPoints, onCheckedChange = { showPoints = it })
            ControlSection(title = "Color")
            ColorRow(label = "Fill color", selected = color, onSelect = { color = it })
        },
    )
}

@Composable
internal fun BarPlayground() {
    var points by remember { mutableStateOf(8) }
    var tick by remember { mutableStateOf(0) }
    var barWidthFraction by remember { mutableStateOf(0.6f) }
    var corner by remember { mutableStateOf<CornerRadius>(CornerRadius.Medium) }
    var color by remember { mutableStateOf(playgroundPalette[3]) }

    val data =
        remember(
            points,
            tick,
        ) { randomValues(points, tick).mapIndexed { i, v -> BarData(label = "B${i + 1}", value = v) } }

    val code =
        """
        val data = listOf(${data.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> BarData("B${'$'}{i + 1}", v) }

        BarChart(
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
            BarChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                color = ChartyColor.Solid(color),
                barConfig =
                    BarChartConfig(
                        barWidthFraction = barWidthFraction,
                        cornerRadius = corner,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Bars", value = points, valueRange = 2..20, onValueChange = { points = it })
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
internal fun PointPlayground() {
    var points by remember { mutableStateOf(14) }
    var tick by remember { mutableStateOf(0) }
    var pointRadius by remember { mutableStateOf(8f) }
    var pointAlpha by remember { mutableStateOf(1f) }
    var showLabels by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf(playgroundPalette[4]) }

    val data =
        remember(
            points,
            tick,
        ) { randomValues(points, tick).mapIndexed { i, v -> PointData(label = "P${i + 1}", value = v) } }

    val code =
        """
        val data = listOf(${data.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> PointData("P${'$'}{i + 1}", v) }

        PointChart(
            data = { data },
            color = ChartyColor.Solid(Color(${colorHex(color)})),
            pointConfig = PointChartConfig(
                pointRadius = ${fc(pointRadius)},
                pointAlpha = ${fc(pointAlpha)},
                showLabels = $showLabels,
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            PointChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                color = ChartyColor.Solid(color),
                pointConfig =
                    PointChartConfig(
                        pointRadius = pointRadius,
                        pointAlpha = pointAlpha,
                        showLabels = showLabels,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Points", value = points, valueRange = 3..40, onValueChange = { points = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Points")
            SliderRow(label = "Radius", value = pointRadius, valueRange = 2f..20f, onValueChange = { pointRadius = it })
            SliderRow(label = "Opacity", value = pointAlpha, valueRange = 0.2f..1f, onValueChange = {
                pointAlpha = it
            }, decimals = 2)
            SwitchRow(label = "Show labels", checked = showLabels, onCheckedChange = { showLabels = it })
            ControlSection(title = "Color")
            ColorRow(label = "Point color", selected = color, onSelect = { color = it })
        },
    )
}

@Composable
internal fun BubblePlayground() {
    var points by remember { mutableStateOf(8) }
    var tick by remember { mutableStateOf(0) }
    var maxRadius by remember { mutableStateOf(50f) }
    var minRadius by remember { mutableStateOf(12f) }
    var color by remember { mutableStateOf(playgroundPalette[1]) }

    val data =
        remember(points, tick) {
            val ys = randomValues(points, tick, 20f, 90f)
            val sizes = randomValues(points, tick + 7, 60f, 300f)
            ys.mapIndexed { i, y -> BubbleData(label = "B${i + 1}", yValue = y, size = sizes[i]) }
        }

    val code =
        """
        val data = listOf(
            ${data.joinToString(
            ",\n            ",
        ) { "BubbleData(\"${it.label}\", yValue = ${fc(it.yValue)}, size = ${fc(it.size)})" }},
        )

        BubbleChart(
            data = { data },
            color = ChartyColor.Solid(Color(${colorHex(color)})),
            config = PointChartConfig(pointRadius = ${fc(maxRadius)}),
            minBubbleRadius = ${fc(minRadius)},
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            BubbleChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                color = ChartyColor.Solid(color),
                config =
                    PointChartConfig(
                        pointRadius = maxRadius,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
                minBubbleRadius = minRadius,
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Bubbles", value = points, valueRange = 3..15, onValueChange = { points = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Size range")
            SliderRow(label = "Max radius", value = maxRadius, valueRange = 30f..80f, onValueChange = {
                maxRadius = it
            }, decimals = 0)
            SliderRow(label = "Min radius", value = minRadius, valueRange = 5f..25f, onValueChange = {
                minRadius = it
            }, decimals = 0)
            ControlSection(title = "Color")
            ColorRow(label = "Bubble color", selected = color, onSelect = { color = it })
        },
    )
}

@Composable
internal fun PiePlayground() {
    var slices by remember { mutableStateOf(5) }
    var tick by remember { mutableStateOf(0) }
    var style by remember { mutableStateOf(PieChartStyle.PIE) }
    var donutHoleRatio by remember { mutableStateOf(0.5f) }
    var sliceSpacing by remember { mutableStateOf(0f) }

    val data =
        remember(slices, tick) {
            randomValues(slices, tick, 10f, 100f).mapIndexed { i, v ->
                PieData(
                    label = "S${i + 1}",
                    value = v,
                    color =
                        ChartyColor.Solid(
                            playgroundPalette[
                                i %
                                    playgroundPalette.size,
                            ],
                        ),
                )
            }
        }

    val code =
        """
        val data = listOf(${data.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> PieData("S${'$'}{i + 1}", v) }

        PieChart(
            data = { data },
            config = PieChartConfig(
                style = PieChartStyle.${style.name},
                donutHoleRatio = ${fc(donutHoleRatio)},
                sliceSpacingDegrees = ${fc(sliceSpacing)},
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            PieChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                config =
                    PieChartConfig(
                        style = style,
                        donutHoleRatio = donutHoleRatio,
                        sliceSpacingDegrees = sliceSpacing,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Slices", value = slices, valueRange = 2..8, onValueChange = { slices = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Shape")
            ChoiceRow(label = "Style", options = PieChartStyle.entries.toList(), selected = style, labelOf = {
                it.name
            }, onSelect = {
                style =
                    it
            })
            if (style == PieChartStyle.DONUT) {
                SliderRow(label = "Hole ratio", value = donutHoleRatio, valueRange = 0f..0.9f, onValueChange = {
                    donutHoleRatio =
                        it
                }, decimals = 2)
            }
            SliderRow(label = "Slice spacing", value = sliceSpacing, valueRange = 0f..10f, onValueChange = {
                sliceSpacing =
                    it
            }, decimals = 1)
        },
    )
}

@Composable
internal fun ComboPlayground() {
    var points by remember { mutableStateOf(8) }
    var tick by remember { mutableStateOf(0) }
    var barWidthFraction by remember { mutableStateOf(0.6f) }
    var lineWidth by remember { mutableStateOf(3f) }
    var showPoints by remember { mutableStateOf(true) }
    var smoothCurve by remember { mutableStateOf(false) }
    var secondaryAxis by remember { mutableStateOf(false) }
    var barColor by remember { mutableStateOf(playgroundPalette[0]) }
    var lineColor by remember { mutableStateOf(playgroundPalette[3]) }

    val data =
        remember(points, tick) {
            val bars = randomValues(points, tick, 40f, 200f)
            val lines = randomValues(points, tick + 7, 40f, 200f)
            bars.mapIndexed { i, b -> ComboChartData(label = "M${i + 1}", barValue = b, lineValue = lines[i]) }
        }

    val code =
        """
        val data = listOf(
            ${data.joinToString(
            ",\n            ",
        ) { "ComboChartData(\"${it.label}\", barValue = ${fc(it.barValue)}, lineValue = ${fc(it.lineValue)})" }},
        )

        ComboChart(
            data = { data },
            barColor = ChartyColor.Solid(Color(${colorHex(barColor)})),
            lineColor = ChartyColor.Solid(Color(${colorHex(lineColor)})),
            comboConfig = ComboChartConfig(
                barWidthFraction = ${fc(barWidthFraction)},
                lineWidth = ${fc(lineWidth)},
                showPoints = $showPoints,
                smoothCurve = $smoothCurve,
                secondaryAxisForLine = $secondaryAxis,
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            ComboChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                barColor = ChartyColor.Solid(barColor),
                lineColor = ChartyColor.Solid(lineColor),
                comboConfig =
                    ComboChartConfig(
                        barWidthFraction = barWidthFraction,
                        lineWidth = lineWidth,
                        showPoints = showPoints,
                        smoothCurve = smoothCurve,
                        secondaryAxisForLine = secondaryAxis,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Groups", value = points, valueRange = 3..14, onValueChange = { points = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Bars & line")
            SliderRow(label = "Bar width fraction", value = barWidthFraction, valueRange = 0.2f..1f, onValueChange = {
                barWidthFraction =
                    it
            }, decimals = 2)
            SliderRow(label = "Line width", value = lineWidth, valueRange = 1f..6f, onValueChange = { lineWidth = it })
            SwitchRow(label = "Show points", checked = showPoints, onCheckedChange = { showPoints = it })
            SwitchRow(label = "Smooth curve", checked = smoothCurve, onCheckedChange = { smoothCurve = it })
            SwitchRow(
                label = "Secondary axis for line",
                checked = secondaryAxis,
                onCheckedChange = { secondaryAxis = it },
            )
            ControlSection(title = "Colors")
            ColorRow(label = "Bar color", selected = barColor, onSelect = { barColor = it })
            ColorRow(label = "Line color", selected = lineColor, onSelect = { lineColor = it })
        },
    )
}
