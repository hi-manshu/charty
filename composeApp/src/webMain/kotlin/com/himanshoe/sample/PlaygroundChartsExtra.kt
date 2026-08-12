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
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.bar.WavyChart
import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.block.BlockBarChart
import com.himanshoe.charty.block.config.BlockBarChartConfig
import com.himanshoe.charty.block.data.BlockData
import com.himanshoe.charty.calendar.CalendarHeatmapChart
import com.himanshoe.charty.calendar.config.CalendarHeatmapConfig
import com.himanshoe.charty.calendar.data.CalendarData
import com.himanshoe.charty.candlestick.CandlestickChart
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig
import com.himanshoe.charty.candlestick.data.CandleData
import com.himanshoe.charty.circular.CircularProgressIndicator
import com.himanshoe.charty.circular.config.CircularProgressConfig
import com.himanshoe.charty.circular.config.RingDirection
import com.himanshoe.charty.circular.data.CircularRingData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.line.MultilineChart
import com.himanshoe.charty.line.StackedAreaChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.config.LineInterpolation
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.radar.RadarChart
import com.himanshoe.charty.radar.config.RadarChartConfig
import com.himanshoe.charty.radar.config.RadarGridConfig
import com.himanshoe.charty.radar.config.RadarGridStyle
import com.himanshoe.charty.radar.data.RadarAxisData
import com.himanshoe.charty.radar.data.RadarDataSet
import kotlin.random.Random

private val radarAxisLabels = listOf("Speed", "Power", "Range", "Agility", "Defense", "Luck", "Focus", "Stamina")

private fun randomGroups(
    points: Int,
    series: Int,
    tick: Int,
): List<LineGroup> {
    val random = Random(seed = tick * 1000 + points * 10 + series)
    return List(
        points,
    ) { p -> LineGroup(label = p.toString(), values = List(series) { 20f + random.nextFloat() * 80f }) }
}

@Composable
internal fun CandlestickPlayground() {
    var candles by remember { mutableStateOf(20) }
    var tick by remember { mutableStateOf(0) }
    var candleWidthFraction by remember { mutableStateOf(0.7f) }
    var wickWidthFraction by remember { mutableStateOf(0.1f) }
    var showWicks by remember { mutableStateOf(true) }
    var bullish by remember { mutableStateOf(playgroundPalette[5]) }
    var bearish by remember { mutableStateOf(playgroundPalette[1]) }

    val data =
        remember(candles, tick) {
            val random = Random(seed = tick * 1000 + candles)
            var prev = 100f
            List(candles) { i ->
                val open = prev
                val close = (open + (random.nextFloat() - 0.5f) * 24f).coerceIn(20f, 200f)
                val high = maxOf(open, close) + random.nextFloat() * 8f
                val low = minOf(open, close) - random.nextFloat() * 8f
                prev = close
                CandleData(label = "D${i + 1}", open = open, high = high, low = low, close = close)
            }
        }

    val code =
        """
        val data = listOf(
            ${data.joinToString(
            ",\n            ",
        ) {
            "CandleData(\"${it.label}\", open = ${fc(
                it.open,
            )}, high = ${fc(it.high)}, low = ${fc(it.low)}, close = ${fc(it.close)})"
        }},
        )

        CandlestickChart(
            data = { data },
            bullishColor = ChartyColor.Solid(Color(${colorHex(bullish)})),
            bearishColor = ChartyColor.Solid(Color(${colorHex(bearish)})),
            candlestickConfig = CandlestickChartConfig(
                candleWidthFraction = ${fc(candleWidthFraction)},
                wickWidthFraction = ${fc(wickWidthFraction)},
                showWicks = $showWicks,
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            CandlestickChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                bullishColor = ChartyColor.Solid(bullish),
                bearishColor = ChartyColor.Solid(bearish),
                candlestickConfig =
                    CandlestickChartConfig(
                        candleWidthFraction = candleWidthFraction,
                        wickWidthFraction = wickWidthFraction,
                        showWicks = showWicks,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Candles", value = candles, valueRange = 5..40, onValueChange = { candles = it })
            PlaygroundActionRow(primaryLabel = "New series", onPrimary = { tick++ })
            ControlSection(title = "Candles")
            SliderRow(label = "Body width", value = candleWidthFraction, valueRange = 0.2f..1f, onValueChange = {
                candleWidthFraction =
                    it
            }, decimals = 2)
            SliderRow(label = "Wick width", value = wickWidthFraction, valueRange = 0.05f..0.3f, onValueChange = {
                wickWidthFraction =
                    it
            }, decimals = 2)
            SwitchRow(label = "Show wicks", checked = showWicks, onCheckedChange = { showWicks = it })
            ControlSection(title = "Colors")
            ColorRow(label = "Bullish", selected = bullish, onSelect = { bullish = it })
            ColorRow(label = "Bearish", selected = bearish, onSelect = { bearish = it })
        },
    )
}

@Composable
internal fun RadarPlayground() {
    var axes by remember { mutableStateOf(6) }
    var tick by remember { mutableStateOf(0) }
    var dataLineWidth by remember { mutableStateOf(2f) }
    var showDataPoints by remember { mutableStateOf(true) }
    var dataPointRadius by remember { mutableStateOf(4f) }
    var fillAlpha by remember { mutableStateOf(0.3f) }
    var gridStyle by remember { mutableStateOf(RadarGridStyle.POLYGON) }
    var color by remember { mutableStateOf(playgroundPalette[0]) }

    val dataSet =
        remember(axes, tick, color, fillAlpha) {
            val random = Random(seed = tick * 1000 + axes)
            RadarDataSet(
                label = "Player",
                axes =
                    List(axes) { i ->
                        RadarAxisData(
                            label = radarAxisLabels[i % radarAxisLabels.size],
                            value =
                                20f + random.nextFloat() * 80f,
                        )
                    },
                color = ChartyColor.Solid(color),
                fillAlpha = fillAlpha,
            )
        }

    val code =
        """
        val dataSet = RadarDataSet(
            label = "Player",
            axes = listOf(
                ${dataSet.axes.joinToString(
            ",\n                ",
        ) { "RadarAxisData(\"${it.label}\", ${fc(it.value)})" }},
            ),
            color = ChartyColor.Solid(Color(${colorHex(color)})),
            fillAlpha = ${fc(fillAlpha)},
        )

        RadarChart(
            data = { listOf(dataSet) },
            config = RadarChartConfig(
                dataLineWidth = ${fc(dataLineWidth)},
                showDataPoints = $showDataPoints,
                dataPointRadius = ${fc(dataPointRadius)},
                gridConfig = RadarGridConfig(gridStyle = RadarGridStyle.${gridStyle.name}),
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            RadarChart(
                modifier = Modifier.fillMaxSize(),
                data = { listOf(dataSet) },
                config =
                    RadarChartConfig(
                        dataLineWidth = dataLineWidth,
                        showDataPoints = showDataPoints,
                        dataPointRadius = dataPointRadius,
                        gridConfig = RadarGridConfig(gridStyle = gridStyle),
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Axes", value = axes, valueRange = 3..8, onValueChange = { axes = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Shape")
            SliderRow(label = "Line width", value = dataLineWidth, valueRange = 1f..5f, onValueChange = {
                dataLineWidth =
                    it
            })
            SwitchRow(label = "Show data points", checked = showDataPoints, onCheckedChange = { showDataPoints = it })
            if (showDataPoints) {
                SliderRow(label = "Point radius", value = dataPointRadius, valueRange = 2f..8f, onValueChange = {
                    dataPointRadius =
                        it
                })
            }
            SliderRow(label = "Fill alpha", value = fillAlpha, valueRange = 0f..1f, onValueChange = {
                fillAlpha = it
            }, decimals = 2)
            ChoiceRow(label = "Grid", options = RadarGridStyle.entries.toList(), selected = gridStyle, labelOf = {
                it.name
            }, onSelect = {
                gridStyle =
                    it
            })
            ControlSection(title = "Color")
            ColorRow(label = "Series color", selected = color, onSelect = { color = it })
        },
    )
}

@Composable
internal fun CircularPlayground() {
    var rings by remember { mutableStateOf(3) }
    var tick by remember { mutableStateOf(0) }
    var gapBetweenRings by remember { mutableStateOf(8f) }
    var centerHoleRatio by remember { mutableStateOf(0f) }
    var direction by remember { mutableStateOf(RingDirection.CLOCKWISE) }

    val data =
        remember(rings, tick) {
            val random = Random(seed = tick * 1000 + rings)
            List(rings) { i ->
                CircularRingData(
                    label = "R${i + 1}",
                    progress = 20f + random.nextFloat() * 80f,
                    color = ChartyColor.Solid(playgroundPalette[i % playgroundPalette.size]),
                )
            }
        }

    val code =
        """
        val rings = listOf(
            ${data.joinToString(
            ",\n            ",
        ) { "CircularRingData(\"${it.label}\", progress = ${fc(it.progress)})" }},
        )

        CircularProgressIndicator(
            rings = { rings },
            config = CircularProgressConfig(
                gapBetweenRings = ${fc(gapBetweenRings)},
                centerHoleRatio = ${fc(centerHoleRatio)},
                ringDirection = RingDirection.${direction.name},
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                rings = { data },
                config =
                    CircularProgressConfig(
                        gapBetweenRings = gapBetweenRings,
                        centerHoleRatio = centerHoleRatio,
                        ringDirection = direction,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Rings", value = rings, valueRange = 1..5, onValueChange = { rings = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Shape")
            SliderRow(label = "Ring gap", value = gapBetweenRings, valueRange = 0f..24f, onValueChange = {
                gapBetweenRings =
                    it
            }, decimals = 0)
            SliderRow(label = "Center hole", value = centerHoleRatio, valueRange = 0f..0.5f, onValueChange = {
                centerHoleRatio =
                    it
            }, decimals = 2)
            ChoiceRow(label = "Direction", options = RingDirection.entries.toList(), selected = direction, labelOf = {
                it.name
            }, onSelect = {
                direction =
                    it
            })
        },
    )
}

@Composable
internal fun BlockPlayground() {
    var blocks by remember { mutableStateOf(5) }
    var tick by remember { mutableStateOf(0) }
    var barHeight by remember { mutableStateOf(16f) }
    var gap by remember { mutableStateOf(4f) }
    var corner by remember {
        mutableStateOf<com.himanshoe.charty.common.config.CornerRadius>(
            com.himanshoe.charty.common.config.CornerRadius.Small,
        )
    }

    val data =
        remember(blocks, tick) {
            randomValues(blocks, tick, 10f, 100f).mapIndexed { i, v ->
                BlockData(value = v, color = ChartyColor.Solid(playgroundPalette[i % playgroundPalette.size]))
            }
        }

    val code =
        """
        val data = listOf(${data.joinToString(", ") { fc(it.value) }})
            .map { BlockData(it, ChartyColor.Solid(Color(0xFF2962FF))) }

        BlockBarChart(
            data = { data },
            blockBarConfig = BlockBarChartConfig(
                cornerRadius = CornerRadius.${cornerName(corner)},
                gapBetweenBlocks = ${fc(gap)}.dp,
                barHeight = ${fc(barHeight)}.dp,
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            BlockBarChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                blockBarConfig =
                    BlockBarChartConfig(
                        cornerRadius = corner,
                        gapBetweenBlocks = gap.dp,
                        barHeight = barHeight.dp,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Blocks", value = blocks, valueRange = 2..8, onValueChange = { blocks = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Shape")
            SliderRow(label = "Bar height", value = barHeight, valueRange = 8f..48f, onValueChange = {
                barHeight = it
            }, decimals = 0)
            SliderRow(label = "Gap", value = gap, valueRange = 0f..16f, onValueChange = { gap = it }, decimals = 0)
            ChoiceRow(label = "Corner radius", options = cornerOptions, selected = corner, labelOf = ::cornerLabel, onSelect = {
                corner =
                    it
            })
        },
    )
}

@Composable
internal fun WavyPlayground() {
    var bars by remember { mutableStateOf(6) }
    var tick by remember { mutableStateOf(0) }
    var barWidthFraction by remember { mutableStateOf(0.8f) }
    var amplitude by remember { mutableStateOf(0.33f) }
    var strokeWidth by remember { mutableStateOf(3f) }
    var color by remember { mutableStateOf(playgroundPalette[6]) }

    val data =
        remember(bars, tick) { randomValues(bars, tick).mapIndexed { i, v -> BarData(label = "W${i + 1}", value = v) } }

    val code =
        """
        val data = listOf(${data.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> BarData("W${'$'}{i + 1}", v) }

        WavyChart(
            data = { data },
            color = ChartyColor.Solid(Color(${colorHex(color)})),
            wavyConfig = WavyChartConfig(
                barWidthFraction = ${fc(barWidthFraction)},
                waveAmplitudeFractionOfBarWidth = ${fc(amplitude)},
                strokeWidthDp = ${fc(strokeWidth)},
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            WavyChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                color = ChartyColor.Solid(color),
                wavyConfig =
                    WavyChartConfig(
                        barWidthFraction = barWidthFraction,
                        waveAmplitudeFractionOfBarWidth = amplitude,
                        strokeWidthDp = strokeWidth,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Bars", value = bars, valueRange = 3..12, onValueChange = { bars = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Wave")
            SliderRow(label = "Width fraction", value = barWidthFraction, valueRange = 0.2f..1f, onValueChange = {
                barWidthFraction =
                    it
            }, decimals = 2)
            SliderRow(
                label = "Amplitude",
                value = amplitude,
                valueRange = 0f..1f,
                onValueChange = { amplitude = it },
                decimals = 2,
            )
            SliderRow(label = "Stroke width", value = strokeWidth, valueRange = 1f..8f, onValueChange = {
                strokeWidth =
                    it
            })
            ControlSection(title = "Color")
            ColorRow(label = "Wave color", selected = color, onSelect = { color = it })
        },
    )
}

@Composable
internal fun MultilinePlayground() {
    var points by remember { mutableStateOf(8) }
    var series by remember { mutableStateOf(3) }
    var tick by remember { mutableStateOf(0) }
    var lineWidth by remember { mutableStateOf(3f) }
    var showPoints by remember { mutableStateOf(true) }
    var showGradientFill by remember { mutableStateOf(false) }
    var interpolation by remember { mutableStateOf(LineInterpolation.SMOOTH) }

    val data = remember(points, series, tick) { randomGroups(points, series, tick) }

    val code =
        """
        val data = listOf(
            ${data.joinToString(
            ",\n            ",
        ) { g -> "LineGroup(\"${g.label}\", listOf(${g.values.joinToString(", ") { fc(it) }}))" }},
        )

        MultilineChart(
            data = { data },
            colors = ChartyColor.Gradient(seriesColors),
            lineConfig = LineChartConfig(
                lineWidth = ${fc(lineWidth)},
                showPoints = $showPoints,
                interpolation = LineInterpolation.${interpolation.name},
                showGradientFill = $showGradientFill,
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            MultilineChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                colors = ChartyColor.Gradient(playgroundPalette.take(series.coerceAtLeast(2))),
                lineConfig =
                    LineChartConfig(
                        lineWidth = lineWidth,
                        showPoints = showPoints,
                        interpolation = interpolation,
                        showGradientFill = showGradientFill,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Points", value = points, valueRange = 4..20, onValueChange = { points = it })
            IntSliderRow(label = "Series", value = series, valueRange = 2..5, onValueChange = { series = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Lines")
            SliderRow(label = "Line width", value = lineWidth, valueRange = 1f..8f, onValueChange = { lineWidth = it })
            ChoiceRow(label = "Interpolation", options = LineInterpolation.entries.toList(), selected = interpolation, labelOf = {
                it.name
            }, onSelect = {
                interpolation =
                    it
            })
            SwitchRow(label = "Show points", checked = showPoints, onCheckedChange = { showPoints = it })
            SwitchRow(label = "Gradient fill", checked = showGradientFill, onCheckedChange = { showGradientFill = it })
        },
    )
}

@Composable
internal fun StackedAreaPlayground() {
    var points by remember { mutableStateOf(8) }
    var series by remember { mutableStateOf(3) }
    var tick by remember { mutableStateOf(0) }
    var lineWidth by remember { mutableStateOf(2f) }
    var fillAlpha by remember { mutableStateOf(0.7f) }
    var interpolation by remember { mutableStateOf(LineInterpolation.SMOOTH) }

    val data = remember(points, series, tick) { randomGroups(points, series, tick) }

    val code =
        """
        val data = listOf(
            ${data.joinToString(
            ",\n            ",
        ) { g -> "LineGroup(\"${g.label}\", listOf(${g.values.joinToString(", ") { fc(it) }}))" }},
        )

        StackedAreaChart(
            data = { data },
            colors = ChartyColor.Gradient(seriesColors),
            lineConfig = LineChartConfig(
                lineWidth = ${fc(lineWidth)},
                interpolation = LineInterpolation.${interpolation.name},
            ),
            fillAlpha = ${fc(fillAlpha)},
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            StackedAreaChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                colors = ChartyColor.Gradient(playgroundPalette.take(series.coerceAtLeast(2))),
                lineConfig =
                    LineChartConfig(
                        lineWidth = lineWidth,
                        interpolation = interpolation,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
                fillAlpha = fillAlpha,
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Points", value = points, valueRange = 4..20, onValueChange = { points = it })
            IntSliderRow(label = "Series", value = series, valueRange = 2..5, onValueChange = { series = it })
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Area")
            SliderRow(label = "Line width", value = lineWidth, valueRange = 1f..6f, onValueChange = { lineWidth = it })
            SliderRow(label = "Fill alpha", value = fillAlpha, valueRange = 0f..1f, onValueChange = {
                fillAlpha = it
            }, decimals = 2)
            ChoiceRow(label = "Interpolation", options = LineInterpolation.entries.toList(), selected = interpolation, labelOf = {
                it.name
            }, onSelect = {
                interpolation =
                    it
            })
        },
    )
}

@Composable
internal fun CalendarPlayground() {
    var tick by remember { mutableStateOf(0) }
    var cellSize by remember { mutableStateOf(14f) }
    var cellSpacing by remember { mutableStateOf(2f) }
    var showMonthLabels by remember { mutableStateOf(true) }
    var showDayLabels by remember { mutableStateOf(true) }

    val data =
        remember(tick) {
            val random = Random(seed = tick * 1000 + 7)
            buildList {
                for (month in 1..3) {
                    for (day in 1..28) {
                        add(CalendarData(year = 2024, month = month, day = day, value = random.nextFloat() * 10f))
                    }
                }
            }
        }

    val code =
        """
        // ${data.size} days (year 2024, months 1-3); first few shown:
        val data = listOf(
            ${data.take(
            4,
        ).joinToString(
            ",\n            ",
        ) { "CalendarData(year = ${it.year}, month = ${it.month}, day = ${it.day}, value = ${fc(it.value)})" }},
            // ...
        )

        CalendarHeatmapChart(
            data = { data },
            config = CalendarHeatmapConfig(
                cellSize = ${fc(cellSize)}.dp,
                cellSpacing = ${fc(cellSpacing)}.dp,
                showMonthLabels = $showMonthLabels,
                showDayLabels = $showDayLabels,
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            CalendarHeatmapChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                config =
                    CalendarHeatmapConfig(
                        cellSize = cellSize.dp,
                        cellSpacing = cellSpacing.dp,
                        showMonthLabels = showMonthLabels,
                        showDayLabels = showDayLabels,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { tick++ })
            ControlSection(title = "Cells")
            SliderRow(
                label = "Cell size",
                value = cellSize,
                valueRange = 8f..24f,
                onValueChange = { cellSize = it },
                decimals = 0,
            )
            SliderRow(label = "Cell spacing", value = cellSpacing, valueRange = 0f..6f, onValueChange = {
                cellSpacing =
                    it
            }, decimals = 0)
            SwitchRow(label = "Month labels", checked = showMonthLabels, onCheckedChange = { showMonthLabels = it })
            SwitchRow(label = "Day labels", checked = showDayLabels, onCheckedChange = { showDayLabels = it })
        },
    )
}
