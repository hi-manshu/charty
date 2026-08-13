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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.himanshoe.charty.calendar.config.CellShape
import com.himanshoe.charty.calendar.config.WeekStartDay
import com.himanshoe.charty.calendar.data.CalendarData
import com.himanshoe.charty.candlestick.CandlestickChart
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig
import com.himanshoe.charty.candlestick.data.CandleData
import com.himanshoe.charty.circular.CircularProgressIndicator
import com.himanshoe.charty.circular.config.CircularProgressConfig
import com.himanshoe.charty.circular.config.RingDirection
import com.himanshoe.charty.circular.data.CircularRingData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.line.MultilineChart
import com.himanshoe.charty.line.StackedAreaChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.config.LineInterpolation
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.radar.RadarChart
import com.himanshoe.charty.radar.config.RadarChartConfig
import com.himanshoe.charty.radar.config.RadarGridConfig
import com.himanshoe.charty.radar.config.RadarGridStyle
import com.himanshoe.charty.radar.config.RadarLabelConfig
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
    var candles by remember { mutableIntStateOf(20) }
    var tick by remember { mutableIntStateOf(0) }
    var candleWidthFraction by remember { mutableFloatStateOf(0.7f) }
    var wickWidthFraction by remember { mutableFloatStateOf(0.1f) }
    var showWicks by remember { mutableStateOf(true) }
    var minBodyHeight by remember { mutableFloatStateOf(2f) }
    var corner by remember { mutableStateOf<CornerRadius>(CornerRadius.None) }
    var animateValueChanges by remember { mutableStateOf(false) }
    var markers by remember { mutableStateOf(false) }
    var window by remember { mutableStateOf(false) }
    var windowSize by remember { mutableIntStateOf(10) }
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
                minCandleBodyHeight = ${fc(minBodyHeight)},
                showWicks = $showWicks,
                cornerRadius = CornerRadius.${cornerName(corner)},${codeArg(
            include = animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = markers,
            text = "markers = listOf(PersistentMarker(dataIndex = ${data.size / 2}, label = \"Peak\", showGuideLine = true)),",
            indent = 16,
        )}${codeArg(
            include = window,
            text = "visibleWindow = $windowSize,",
            indent = 16,
        )}
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            CandlestickChart(
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                interactionConfig = playgroundInteractionConfig(),
                data = { data },
                bullishColor = ChartyColor.Solid(bullish),
                bearishColor = ChartyColor.Solid(bearish),
                candlestickConfig =
                    CandlestickChartConfig(
                        candleWidthFraction = candleWidthFraction,
                        wickWidthFraction = wickWidthFraction,
                        minCandleBodyHeight = minBodyHeight,
                        showWicks = showWicks,
                        cornerRadius = corner,
                        animation = playgroundAnimation(animate = LocalPlaygroundAnimate.current),
                        animateValueChanges = animateValueChanges,
                        markers = demoMarkers(enabled = markers, size = data.size, label = "Peak"),
                        visibleWindow = windowOf(enabled = window, size = windowSize),
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
            SliderRow(
                label = "Min body height",
                value = minBodyHeight,
                valueRange = 0f..12f,
                onValueChange = { minBodyHeight = it },
                decimals = 0,
            )
            CornerRadiusControls(label = "Corner radius", corner = corner, onCornerChange = { corner = it })
            ControlSection(title = "Behaviour")
            SwitchRow(
                label = "Animate value changes",
                checked = animateValueChanges,
                onCheckedChange = { animateValueChanges = it },
            )
            SwitchRow(label = "Persistent marker", checked = markers, onCheckedChange = { markers = it })
            VisibleWindowControls(
                enabled = window,
                onEnabledChange = { window = it },
                size = windowSize,
                onSizeChange = { windowSize = it },
                sizeRange = 3..40,
            )
            ControlSection(title = "Colors")
            ColorRow(label = "Bullish", selected = bullish, onSelect = { bullish = it })
            ColorRow(label = "Bearish", selected = bearish, onSelect = { bearish = it })
        },
    )
}

@Composable
internal fun RadarPlayground() {
    var axes by remember { mutableIntStateOf(6) }
    var tick by remember { mutableIntStateOf(0) }
    var dataLineWidth by remember { mutableFloatStateOf(2f) }
    var showDataPoints by remember { mutableStateOf(true) }
    var dataPointRadius by remember { mutableFloatStateOf(4f) }
    var fillAlpha by remember { mutableFloatStateOf(0.3f) }
    var gridStyle by remember { mutableStateOf(RadarGridStyle.POLYGON) }
    var gridLevels by remember { mutableIntStateOf(5) }
    var showGridLines by remember { mutableStateOf(true) }
    var showAxisLines by remember { mutableStateOf(true) }
    var showLabels by remember { mutableStateOf(true) }
    var showValues by remember { mutableStateOf(false) }
    var startAngle by remember { mutableFloatStateOf(-90f) }
    var paddingFraction by remember { mutableFloatStateOf(0.15f) }
    var scaleToFit by remember { mutableStateOf(true) }
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
                startAngleDegrees = ${fc(startAngle)},
                scaleToFit = $scaleToFit,
                paddingFraction = ${fc(paddingFraction)},
                labelConfig = RadarLabelConfig(showLabels = $showLabels, showValues = $showValues),
                gridConfig = RadarGridConfig(
                    gridStyle = RadarGridStyle.${gridStyle.name},
                    numberOfGridLevels = $gridLevels,
                    showGridLines = $showGridLines,
                    showAxisLines = $showAxisLines,
                ),
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        cartesian = false,
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
                        startAngleDegrees = startAngle,
                        scaleToFit = scaleToFit,
                        paddingFraction = paddingFraction,
                        labelConfig = RadarLabelConfig(showLabels = showLabels, showValues = showValues),
                        gridConfig =
                            RadarGridConfig(
                                gridStyle = gridStyle,
                                numberOfGridLevels = gridLevels,
                                showGridLines = showGridLines,
                                showAxisLines = showAxisLines,
                            ),
                        animation = playgroundAnimation(animate = LocalPlaygroundAnimate.current),
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
            SliderRow(
                label = "Start angle",
                value = startAngle,
                valueRange = -180f..180f,
                onValueChange = { startAngle = it },
                decimals = 0,
            )
            ControlSection(title = "Grid")
            ChoiceRow(
                label = "Grid",
                options = RadarGridStyle.entries.toList(),
                selected = gridStyle,
                labelOf = { it.name },
                onSelect = { gridStyle = it },
            )
            IntSliderRow(label = "Grid levels", value = gridLevels, valueRange = 1..10, onValueChange = {
                gridLevels = it
            })
            SwitchRow(label = "Grid lines", checked = showGridLines, onCheckedChange = { showGridLines = it })
            SwitchRow(label = "Axis lines", checked = showAxisLines, onCheckedChange = { showAxisLines = it })
            ControlSection(title = "Labels & layout")
            SwitchRow(label = "Axis labels", checked = showLabels, onCheckedChange = { showLabels = it })
            SwitchRow(label = "Axis values", checked = showValues, onCheckedChange = { showValues = it })
            SwitchRow(label = "Scale to fit", checked = scaleToFit, onCheckedChange = { scaleToFit = it })
            SliderRow(
                label = "Padding fraction",
                value = paddingFraction,
                valueRange = 0f..0.5f,
                onValueChange = { paddingFraction = it },
                decimals = 2,
            )
            ControlSection(title = "Color")
            ColorRow(label = "Series color", selected = color, onSelect = { color = it })
        },
    )
}

@Composable
internal fun CircularPlayground() {
    var rings by remember { mutableIntStateOf(3) }
    var tick by remember { mutableIntStateOf(0) }
    var gapBetweenRings by remember { mutableFloatStateOf(8f) }
    var centerHoleRatio by remember { mutableFloatStateOf(0f) }
    var direction by remember { mutableStateOf(RingDirection.CLOCKWISE) }
    var startAngle by remember { mutableFloatStateOf(-90f) }
    var padding by remember { mutableFloatStateOf(16f) }
    var enableShadows by remember { mutableStateOf(false) }
    var rotationEnabled by remember { mutableStateOf(false) }
    var showCenterText by remember { mutableStateOf(false) }
    var interactionEnabled by remember { mutableStateOf(true) }

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
                startAngleDegrees = ${fc(startAngle)},
                paddingDp = ${fc(padding)},
                enableShadows = $enableShadows,
                rotationEnabled = $rotationEnabled,
                showCenterText = $showCenterText,
                interactionEnabled = $interactionEnabled,
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        cartesian = false,
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
                        startAngleDegrees = startAngle,
                        paddingDp = padding,
                        enableShadows = enableShadows,
                        rotationEnabled = rotationEnabled,
                        showCenterText = showCenterText,
                        interactionEnabled = interactionEnabled,
                        animation = playgroundAnimation(animate = LocalPlaygroundAnimate.current),
                    ),
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(label = "Rings", value = rings, valueRange = 1..8, onValueChange = { rings = it })
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
            ChoiceRow(
                label = "Direction",
                options = RingDirection.entries.toList(),
                selected = direction,
                labelOf = { it.name },
                onSelect = { direction = it },
            )
            SliderRow(
                label = "Start angle",
                value = startAngle,
                valueRange = -180f..180f,
                onValueChange = { startAngle = it },
                decimals = 0,
            )
            SliderRow(
                label = "Padding",
                value = padding,
                valueRange = 0f..48f,
                onValueChange = { padding = it },
                decimals = 0,
            )
            ControlSection(title = "Behaviour")
            SwitchRow(label = "Shadows", checked = enableShadows, onCheckedChange = { enableShadows = it })
            SwitchRow(label = "Rotate rings", checked = rotationEnabled, onCheckedChange = { rotationEnabled = it })
            SwitchRow(label = "Center text", checked = showCenterText, onCheckedChange = { showCenterText = it })
            SwitchRow(
                label = "Tap interaction",
                checked = interactionEnabled,
                onCheckedChange = { interactionEnabled = it },
            )
        },
    )
}

@Composable
internal fun BlockPlayground() {
    var blocks by remember { mutableIntStateOf(5) }
    var tick by remember { mutableIntStateOf(0) }
    var barHeight by remember { mutableFloatStateOf(16f) }
    var gap by remember { mutableFloatStateOf(4f) }
    var corner by remember { mutableStateOf<CornerRadius>(CornerRadius.Small) }

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
        cartesian = false,
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
            CornerRadiusControls(label = "Corner radius", corner = corner, onCornerChange = { corner = it })
        },
    )
}

@Composable
internal fun WavyPlayground() {
    var bars by remember { mutableIntStateOf(6) }
    var tick by remember { mutableIntStateOf(0) }
    var barWidthFraction by remember { mutableFloatStateOf(0.8f) }
    var amplitude by remember { mutableFloatStateOf(0.33f) }
    var strokeWidth by remember { mutableFloatStateOf(3f) }
    var waveSegments by remember { mutableIntStateOf(40) }
    var phaseOffset by remember { mutableFloatStateOf(0f) }
    var animateValueChanges by remember { mutableStateOf(false) }
    var markers by remember { mutableStateOf(false) }
    var window by remember { mutableStateOf(false) }
    var windowSize by remember { mutableIntStateOf(4) }
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
                waveSegments = $waveSegments,
                phaseOffsetPerBar = ${fc(phaseOffset)},
                strokeWidthDp = ${fc(strokeWidth)},${codeArg(
            include = animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = markers,
            text = "markers = listOf(PersistentMarker(dataIndex = ${data.size / 2}, label = \"Peak\", showGuideLine = true)),",
            indent = 16,
        )}${codeArg(
            include = window,
            text = "visibleWindow = $windowSize,",
            indent = 16,
        )}
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            WavyChart(
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                interactionConfig = playgroundInteractionConfig(),
                data = { data },
                color = ChartyColor.Solid(color),
                wavyConfig =
                    WavyChartConfig(
                        barWidthFraction = barWidthFraction,
                        waveAmplitudeFractionOfBarWidth = amplitude,
                        waveSegments = waveSegments,
                        phaseOffsetPerBar = phaseOffset,
                        strokeWidthDp = strokeWidth,
                        animateValueChanges = animateValueChanges,
                        markers = demoMarkers(enabled = markers, size = data.size, label = "Peak"),
                        visibleWindow = windowOf(enabled = window, size = windowSize),
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
            IntSliderRow(label = "Wave segments", value = waveSegments, valueRange = 4..80, onValueChange = {
                waveSegments = it
            })
            SliderRow(
                label = "Phase offset per bar",
                value = phaseOffset,
                valueRange = 0f..6.28f,
                onValueChange = { phaseOffset = it },
                decimals = 2,
            )
            ControlSection(title = "Behaviour")
            SwitchRow(
                label = "Animate value changes",
                checked = animateValueChanges,
                onCheckedChange = { animateValueChanges = it },
            )
            SwitchRow(label = "Persistent marker", checked = markers, onCheckedChange = { markers = it })
            VisibleWindowControls(
                enabled = window,
                onEnabledChange = { window = it },
                size = windowSize,
                onSizeChange = { windowSize = it },
                sizeRange = 2..12,
            )
            ControlSection(title = "Color")
            ColorRow(label = "Wave color", selected = color, onSelect = { color = it })
        },
    )
}

@Composable
internal fun MultilinePlayground() {
    var points by remember { mutableIntStateOf(8) }
    var series by remember { mutableIntStateOf(3) }
    var tick by remember { mutableIntStateOf(0) }
    var lineWidth by remember { mutableFloatStateOf(3f) }
    var showPoints by remember { mutableStateOf(true) }
    var showGradientFill by remember { mutableStateOf(false) }
    var gradientFillAlpha by remember { mutableFloatStateOf(0.3f) }
    var pointRadius by remember { mutableFloatStateOf(6f) }
    var legend by remember { mutableStateOf(false) }
    var animateValueChanges by remember { mutableStateOf(false) }
    var window by remember { mutableStateOf(false) }
    var windowSize by remember { mutableIntStateOf(6) }
    var interpolation by remember { mutableStateOf(LineInterpolation.SMOOTH) }

    val data = remember(points, series, tick) { randomGroups(points, series, tick) }
    val legendLabels = remember(series, legend) { multilineLegend(enabled = legend, series = series) }

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
                pointRadius = ${fc(pointRadius)},
                interpolation = LineInterpolation.${interpolation.name},
                showGradientFill = $showGradientFill,${codeArg(
            include = showGradientFill,
            text = "gradientFillAlpha = ${fc(gradientFillAlpha)},",
            indent = 16,
        )}${codeArg(
            include = legend,
            text = "legendLabels = listOf(${legendLabels.joinToString(", ") { "\"$it\"" }}),",
            indent = 16,
        )}${codeArg(
            include = animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = window,
            text = "visibleWindow = $windowSize,",
            indent = 16,
        )}
            ),
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            MultilineChart(
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                interactionConfig = playgroundInteractionConfig(),
                data = { data },
                colors = ChartyColor.Gradient(playgroundPalette.take(series.coerceAtLeast(2))),
                lineConfig =
                    LineChartConfig(
                        lineWidth = lineWidth,
                        showPoints = showPoints,
                        pointRadius = pointRadius,
                        interpolation = interpolation,
                        showGradientFill = showGradientFill,
                        gradientFillAlpha = gradientFillAlpha,
                        legendLabels = legendLabels,
                        animateValueChanges = animateValueChanges,
                        visibleWindow = windowOf(enabled = window, size = windowSize),
                        animation = playgroundAnimation(animate = LocalPlaygroundAnimate.current),
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
            if (showPoints) {
                SliderRow(
                    label = "Point radius",
                    value = pointRadius,
                    valueRange = 2f..14f,
                    onValueChange = { pointRadius = it },
                )
            }
            SwitchRow(label = "Gradient fill", checked = showGradientFill, onCheckedChange = { showGradientFill = it })
            if (showGradientFill) {
                SliderRow(
                    label = "Gradient alpha",
                    value = gradientFillAlpha,
                    valueRange = 0f..1f,
                    onValueChange = { gradientFillAlpha = it },
                    decimals = 2,
                )
            }
            ControlSection(title = "Legend & behaviour")
            SwitchRow(label = "Legend labels", checked = legend, onCheckedChange = { legend = it })
            SwitchRow(
                label = "Animate value changes",
                checked = animateValueChanges,
                onCheckedChange = { animateValueChanges = it },
            )
            VisibleWindowControls(
                enabled = window,
                onEnabledChange = { window = it },
                size = windowSize,
                onSizeChange = { windowSize = it },
                sizeRange = 2..20,
            )
        },
    )
}

private fun multilineLegend(
    enabled: Boolean,
    series: Int,
): List<String> =
    if (enabled) {
        List(series) { "Series ${it + 1}" }
    } else {
        emptyList()
    }

@Composable
internal fun StackedAreaPlayground() {
    var points by remember { mutableIntStateOf(8) }
    var series by remember { mutableIntStateOf(3) }
    var tick by remember { mutableIntStateOf(0) }
    var lineWidth by remember { mutableFloatStateOf(2f) }
    var fillAlpha by remember { mutableFloatStateOf(0.7f) }
    var showPoints by remember { mutableStateOf(false) }
    var pointRadius by remember { mutableFloatStateOf(5f) }
    var animateValueChanges by remember { mutableStateOf(false) }
    var window by remember { mutableStateOf(false) }
    var windowSize by remember { mutableIntStateOf(6) }
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
                showPoints = $showPoints,
                pointRadius = ${fc(pointRadius)},
                interpolation = LineInterpolation.${interpolation.name},${codeArg(
            include = animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = window,
            text = "visibleWindow = $windowSize,",
            indent = 16,
        )}
            ),
            fillAlpha = ${fc(fillAlpha)},
        )
        """.trimIndent()
    PlaygroundScaffold(
        code = code,
        chart = {
            StackedAreaChart(
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                interactionConfig = playgroundInteractionConfig(),
                data = { data },
                colors = ChartyColor.Gradient(playgroundPalette.take(series.coerceAtLeast(2))),
                lineConfig =
                    LineChartConfig(
                        lineWidth = lineWidth,
                        showPoints = showPoints,
                        pointRadius = pointRadius,
                        interpolation = interpolation,
                        animateValueChanges = animateValueChanges,
                        visibleWindow = windowOf(enabled = window, size = windowSize),
                        animation = playgroundAnimation(animate = LocalPlaygroundAnimate.current),
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
            SwitchRow(label = "Show points", checked = showPoints, onCheckedChange = { showPoints = it })
            if (showPoints) {
                SliderRow(
                    label = "Point radius",
                    value = pointRadius,
                    valueRange = 2f..12f,
                    onValueChange = { pointRadius = it },
                )
            }
            ControlSection(title = "Behaviour")
            SwitchRow(
                label = "Animate value changes",
                checked = animateValueChanges,
                onCheckedChange = { animateValueChanges = it },
            )
            VisibleWindowControls(
                enabled = window,
                onEnabledChange = { window = it },
                size = windowSize,
                onSizeChange = { windowSize = it },
                sizeRange = 2..20,
            )
        },
    )
}

@Composable
internal fun CalendarPlayground() {
    var tick by remember { mutableIntStateOf(0) }
    var cellSize by remember { mutableFloatStateOf(14f) }
    var cellSpacing by remember { mutableFloatStateOf(2f) }
    var showMonthLabels by remember { mutableStateOf(true) }
    var showDayLabels by remember { mutableStateOf(true) }
    var cellCorner by remember { mutableFloatStateOf(2f) }
    var weekStart by remember { mutableStateOf(WeekStartDay.SUNDAY) }
    var limitWeeks by remember { mutableStateOf(false) }
    var visibleWeeks by remember { mutableIntStateOf(8) }
    var scrollEnabled by remember { mutableStateOf(true) }

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
                cellShape = CellShape.RoundedSquare(cornerRadius = ${fc(cellCorner)}),
                showMonthLabels = $showMonthLabels,
                showDayLabels = $showDayLabels,
                weekStartDay = WeekStartDay.${weekStart.name},
            ),${codeArg(include = limitWeeks, text = "visibleWeeks = $visibleWeeks,")}
            scrollEnabled = $scrollEnabled,
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
                        cellShape = CellShape.RoundedSquare(cornerRadius = cellCorner),
                        showMonthLabels = showMonthLabels,
                        showDayLabels = showDayLabels,
                        weekStartDay = weekStart,
                        animation = playgroundAnimation(animate = LocalPlaygroundAnimate.current),
                    ),
                visibleWeeks = windowOf(enabled = limitWeeks, size = visibleWeeks),
                scrollEnabled = scrollEnabled,
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
            SliderRow(
                label = "Cell corner",
                value = cellCorner,
                valueRange = 0f..10f,
                onValueChange = { cellCorner = it },
                decimals = 0,
            )
            ControlSection(title = "Labels & layout")
            SwitchRow(label = "Month labels", checked = showMonthLabels, onCheckedChange = { showMonthLabels = it })
            SwitchRow(label = "Day labels", checked = showDayLabels, onCheckedChange = { showDayLabels = it })
            ChoiceRow(
                label = "Week starts on",
                options = WeekStartDay.entries.toList(),
                selected = weekStart,
                labelOf = { it.name },
                onSelect = { weekStart = it },
            )
            SwitchRow(label = "Limit visible weeks", checked = limitWeeks, onCheckedChange = { limitWeeks = it })
            if (limitWeeks) {
                IntSliderRow(
                    label = "Visible weeks",
                    value = visibleWeeks,
                    valueRange = 2..20,
                    onValueChange = { visibleWeeks = it },
                )
            }
            SwitchRow(label = "Scroll enabled", checked = scrollEnabled, onCheckedChange = { scrollEnabled = it })
        },
    )
}
