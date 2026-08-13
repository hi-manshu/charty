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
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.combo.ComboChart
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.ReferenceBandConfig
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.line.AreaChart
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.config.LineInterpolation
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.pie.PieChart
import com.himanshoe.charty.pie.config.InteractionConfig
import com.himanshoe.charty.pie.config.LabelConfig
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
        else -> "Custom(radius = ${fc(corner.value)})"
    }

internal fun playgroundAnimation(animate: Boolean): Animation =
    if (animate) {
        Animation.Default
    } else {
        Animation.Disabled
    }

internal fun signedValues(
    count: Int,
    tick: Int,
    negatives: Boolean,
): List<Float> =
    if (negatives) {
        randomValues(count = count, tick = tick, min = -80f, max = 100f)
    } else {
        randomValues(count = count, tick = tick)
    }

internal fun demoMarkers(
    enabled: Boolean,
    size: Int,
    label: String,
): List<PersistentMarker> =
    if (enabled && size > 0) {
        listOf(
            PersistentMarker(
                dataIndex = size / 2,
                label = label,
                showGuideLine = true,
            ),
        )
    } else {
        emptyList()
    }

internal fun demoReferenceLine(
    enabled: Boolean,
    value: Float,
): ReferenceLineConfig? =
    if (enabled) {
        ReferenceLineConfig(value = value, label = "Target")
    } else {
        null
    }

internal fun demoReferenceBand(
    enabled: Boolean,
    low: Float,
    high: Float,
): ReferenceBandConfig? =
    if (enabled) {
        ReferenceBandConfig(lowValue = low, highValue = high, label = "Healthy")
    } else {
        null
    }

internal fun demoCrosshair(
    enabled: Boolean,
    showHorizontalLine: Boolean,
    dotRadius: Float,
): ChartCrosshairConfig? =
    if (enabled) {
        ChartCrosshairConfig(showHorizontalLine = showHorizontalLine, dotRadius = dotRadius)
    } else {
        null
    }

private class LineState {
    var points by mutableStateOf(12)
    var tick by mutableStateOf(0)
    var negatives by mutableStateOf(false)
    var lineWidth by mutableStateOf(3f)
    var pointRadius by mutableStateOf(6f)
    var pointAlpha by mutableStateOf(1f)
    var showPoints by mutableStateOf(true)
    var interpolation by mutableStateOf(LineInterpolation.LINEAR)
    var color by mutableStateOf(playgroundPalette[0])
    var showGradientFill by mutableStateOf(false)
    var gradientFillAlpha by mutableStateOf(0.3f)
    var animateValueChanges by mutableStateOf(false)
    var highlightSelectedColumn by mutableStateOf(false)
    var negativeMode by mutableStateOf(NegativeValuesDrawMode.BELOW_AXIS)
    var referenceLine by mutableStateOf(false)
    var referenceValue by mutableStateOf(60f)
    var referenceBand by mutableStateOf(false)
    var bandLow by mutableStateOf(30f)
    var bandHigh by mutableStateOf(70f)
    var markers by mutableStateOf(false)
    var legend by mutableStateOf(false)
    var crosshair by mutableStateOf(false)
    var crosshairHorizontal by mutableStateOf(true)
    var crosshairDot by mutableStateOf(8f)
    var tooltipPosition by mutableStateOf(TooltipPosition.AUTO)
    var downsample by mutableStateOf(false)
    var downsampleThreshold by mutableStateOf(20)
    var window by mutableStateOf(false)
    var windowSize by mutableStateOf(8)
    var forceEmpty by mutableStateOf(false)
    var customEmpty by mutableStateOf(true)
}

@Composable
internal fun LinePlayground() {
    val state = remember { LineState() }
    val animate = LocalPlaygroundAnimate.current
    val series =
        remember(state.points, state.tick, state.negatives) {
            signedValues(count = state.points, tick = state.tick, negatives = state.negatives)
                .mapIndexed { i, v -> LineData(label = i.toString(), value = v) }
        }
    val data =
        if (state.forceEmpty) {
            emptyList()
        } else {
            series
        }

    val code =
        """
        val data = listOf(${series.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> LineData(i.toString(), v) }

        LineChart(
            data = { data },${emptyContentArg(custom = state.customEmpty)}
            color = ChartyColor.Solid(Color(${colorHex(state.color)})),${codeArg(
            include = state.crosshair,
            text = "crosshair = ChartCrosshair(config = ChartCrosshairConfig(showHorizontalLine = ${state.crosshairHorizontal}, dotRadius = ${fc(
                state.crosshairDot,
            )})),",
        )}
            lineConfig = LineChartConfig(
                lineWidth = ${fc(state.lineWidth)},
                showPoints = ${state.showPoints},
                pointRadius = ${fc(state.pointRadius)},
                pointAlpha = ${fc(state.pointAlpha)},
                interpolation = LineInterpolation.${state.interpolation.name},${codeArg(
            include = state.negatives,
            text = "negativeValuesDrawMode = NegativeValuesDrawMode.${state.negativeMode.name},",
            indent = 16,
        )}${codeArg(
            include = state.animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = state.showGradientFill,
            text = "showGradientFill = true,\n                gradientFillAlpha = ${fc(state.gradientFillAlpha)},",
            indent = 16,
        )}${codeArg(
            include = state.highlightSelectedColumn,
            text = "highlightSelectedColumn = true,",
            indent = 16,
        )}${codeArg(
            include = state.legend,
            text = "legendLabels = listOf(\"Series A\"),",
            indent = 16,
        )}${codeArg(
            include = state.referenceLine,
            text = "referenceLine = ReferenceLineConfig(value = ${fc(state.referenceValue)}, label = \"Target\"),",
            indent = 16,
        )}${codeArg(
            include = state.referenceBand,
            text = "referenceBand = ReferenceBandConfig(lowValue = ${fc(
                state.bandLow,
            )}, highValue = ${fc(state.bandHigh)}, label = \"Healthy\"),",
            indent = 16,
        )}${codeArg(
            include = state.markers,
            text = "markers = listOf(PersistentMarker(dataIndex = ${series.size / 2}, label = \"Peak\", showGuideLine = true)),",
            indent = 16,
        )}${codeArg(
            include = state.tooltipPosition != TooltipPosition.AUTO,
            text = "tooltipPosition = TooltipPosition.${state.tooltipPosition.name},",
            indent = 16,
        )}${codeArg(
            include = state.downsample,
            text = "downsampleThreshold = ${state.downsampleThreshold},",
            indent = 16,
        )}${codeArg(
            include = state.window,
            text = "visibleWindow = ${state.windowSize},",
            indent = 16,
        )}
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            LineChart(
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                data = { data },
                emptyContent = emptyPlaceholder(custom = state.customEmpty, onAction = { state.forceEmpty = false }),
                color = ChartyColor.Solid(state.color),
                crosshair =
                    demoCrosshair(
                        enabled = state.crosshair,
                        showHorizontalLine = state.crosshairHorizontal,
                        dotRadius = state.crosshairDot,
                    )?.let { ChartCrosshair(config = it) },
                lineConfig =
                    LineChartConfig(
                        lineWidth = state.lineWidth,
                        showPoints = state.showPoints,
                        pointRadius = state.pointRadius,
                        pointAlpha = state.pointAlpha,
                        interpolation = state.interpolation,
                        negativeValuesDrawMode = state.negativeMode,
                        animation = playgroundAnimation(animate = animate),
                        animateValueChanges = state.animateValueChanges,
                        referenceLine = demoReferenceLine(enabled = state.referenceLine, value = state.referenceValue),
                        referenceBand =
                            demoReferenceBand(
                                enabled = state.referenceBand,
                                low = state.bandLow,
                                high = state.bandHigh,
                            ),
                        markers = demoMarkers(enabled = state.markers, size = series.size, label = "Peak"),
                        tooltipPosition = state.tooltipPosition,
                        highlightSelectedColumn = state.highlightSelectedColumn,
                        legendLabels = legendLabelsOf(enabled = state.legend),
                        showGradientFill = state.showGradientFill,
                        gradientFillAlpha = state.gradientFillAlpha,
                        downsampleThreshold =
                            thresholdOf(
                                enabled = state.downsample,
                                value = state.downsampleThreshold,
                            ),
                        visibleWindow = windowOf(enabled = state.window, size = state.windowSize),
                    ),
            )
        },
        controls = { LineControls(state = state) },
    )
}

@Composable
private fun LineControls(state: LineState) {
    ControlSection(title = "Data")
    IntSliderRow(label = "Points", value = state.points, valueRange = 3..60, onValueChange = { state.points = it })
    SwitchRow(
        label = "Include negatives",
        checked = state.negatives,
        onCheckedChange = { state.negatives = it },
    )
    if (state.negatives) {
        ChoiceRow(
            label = "Negative values",
            options = NegativeValuesDrawMode.entries.toList(),
            selected = state.negativeMode,
            labelOf = { it.name },
            onSelect = { state.negativeMode = it },
        )
    }
    PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { state.tick++ })
    EmptyStateControls(
        forceEmpty = state.forceEmpty,
        onForceEmptyChange = { state.forceEmpty = it },
        custom = state.customEmpty,
        onCustomChange = { state.customEmpty = it },
    )
    ControlSection(title = "Line")
    SliderRow(
        label = "Line width",
        value = state.lineWidth,
        valueRange = 1f..8f,
        onValueChange = { state.lineWidth = it },
    )
    ChoiceRow(
        label = "Interpolation",
        options = LineInterpolation.entries.toList(),
        selected = state.interpolation,
        labelOf = { it.name },
        onSelect = { state.interpolation = it },
    )
    SwitchRow(label = "Show points", checked = state.showPoints, onCheckedChange = { state.showPoints = it })
    if (state.showPoints) {
        SliderRow(
            label = "Point radius",
            value = state.pointRadius,
            valueRange = 2f..14f,
            onValueChange = { state.pointRadius = it },
        )
        SliderRow(
            label = "Point opacity",
            value = state.pointAlpha,
            valueRange = 0.1f..1f,
            onValueChange = { state.pointAlpha = it },
            decimals = 2,
        )
    }
    ControlSection(title = "Fill")
    SwitchRow(
        label = "Gradient fill",
        checked = state.showGradientFill,
        onCheckedChange = { state.showGradientFill = it },
    )
    if (state.showGradientFill) {
        SliderRow(
            label = "Gradient alpha",
            value = state.gradientFillAlpha,
            valueRange = 0f..1f,
            onValueChange = { state.gradientFillAlpha = it },
            decimals = 2,
        )
    }
    ControlSection(title = "Annotations")
    ReferenceLineControls(
        enabled = state.referenceLine,
        onEnabledChange = { state.referenceLine = it },
        value = state.referenceValue,
        onValueChange = { state.referenceValue = it },
        valueRange = 0f..100f,
    )
    ReferenceBandControls(
        enabled = state.referenceBand,
        onEnabledChange = { state.referenceBand = it },
        low = state.bandLow,
        onLowChange = { state.bandLow = it },
        high = state.bandHigh,
        onHighChange = { state.bandHigh = it },
        valueRange = 0f..100f,
    )
    SwitchRow(label = "Persistent marker", checked = state.markers, onCheckedChange = { state.markers = it })
    SwitchRow(label = "Legend label", checked = state.legend, onCheckedChange = { state.legend = it })
    ControlSection(title = "Interaction")
    SwitchRow(label = "Crosshair", checked = state.crosshair, onCheckedChange = { state.crosshair = it })
    if (state.crosshair) {
        SwitchRow(
            label = "Horizontal line",
            checked = state.crosshairHorizontal,
            onCheckedChange = { state.crosshairHorizontal = it },
        )
        SliderRow(
            label = "Crosshair dot",
            value = state.crosshairDot,
            valueRange = 0f..16f,
            onValueChange = { state.crosshairDot = it },
            decimals = 0,
        )
    }
    SwitchRow(
        label = "Highlight column",
        checked = state.highlightSelectedColumn,
        onCheckedChange = { state.highlightSelectedColumn = it },
    )
    ChoiceRow(
        label = "Tooltip position",
        options = TooltipPosition.entries.toList(),
        selected = state.tooltipPosition,
        labelOf = { it.name },
        onSelect = { state.tooltipPosition = it },
    )
    ControlSection(title = "Performance")
    SwitchRow(
        label = "Animate value changes",
        checked = state.animateValueChanges,
        onCheckedChange = { state.animateValueChanges = it },
    )
    SwitchRow(label = "Downsample", checked = state.downsample, onCheckedChange = { state.downsample = it })
    if (state.downsample) {
        IntSliderRow(
            label = "Threshold",
            value = state.downsampleThreshold,
            valueRange = 3..60,
            onValueChange = { state.downsampleThreshold = it },
        )
    }
    VisibleWindowControls(
        enabled = state.window,
        onEnabledChange = { state.window = it },
        size = state.windowSize,
        onSizeChange = { state.windowSize = it },
        sizeRange = 2..30,
    )
    ControlSection(title = "Color")
    ColorRow(label = "Line color", selected = state.color, onSelect = { state.color = it })
}

private class AreaState {
    var points by mutableStateOf(10)
    var tick by mutableStateOf(0)
    var lineWidth by mutableStateOf(3f)
    var fillAlpha by mutableStateOf(0.3f)
    var showPoints by mutableStateOf(false)
    var pointRadius by mutableStateOf(6f)
    var interpolation by mutableStateOf(LineInterpolation.SMOOTH)
    var color by mutableStateOf(playgroundPalette[2])
    var animateValueChanges by mutableStateOf(false)
    var referenceLine by mutableStateOf(false)
    var referenceValue by mutableStateOf(60f)
    var markers by mutableStateOf(false)
    var window by mutableStateOf(false)
    var windowSize by mutableStateOf(8)
    var forceEmpty by mutableStateOf(false)
    var customEmpty by mutableStateOf(true)
}

@Composable
internal fun AreaPlayground() {
    val state = remember { AreaState() }
    val animate = LocalPlaygroundAnimate.current
    val series =
        remember(state.points, state.tick) {
            randomValues(count = state.points, tick = state.tick)
                .mapIndexed { i, v -> LineData(label = i.toString(), value = v) }
        }
    val data =
        if (state.forceEmpty) {
            emptyList()
        } else {
            series
        }

    val code =
        """
        val data = listOf(${series.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> LineData(i.toString(), v) }

        AreaChart(
            data = { data },${emptyContentArg(custom = state.customEmpty)}
            color = ChartyColor.Solid(Color(${colorHex(state.color)})),
            lineConfig = LineChartConfig(
                lineWidth = ${fc(state.lineWidth)},
                showPoints = ${state.showPoints},
                pointRadius = ${fc(state.pointRadius)},
                interpolation = LineInterpolation.${state.interpolation.name},
                fillAlpha = ${fc(state.fillAlpha)},${codeArg(
            include = state.animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = state.referenceLine,
            text = "referenceLine = ReferenceLineConfig(value = ${fc(state.referenceValue)}, label = \"Target\"),",
            indent = 16,
        )}${codeArg(
            include = state.markers,
            text = "markers = listOf(PersistentMarker(dataIndex = ${series.size / 2}, label = \"Peak\", showGuideLine = true)),",
            indent = 16,
        )}${codeArg(
            include = state.window,
            text = "visibleWindow = ${state.windowSize},",
            indent = 16,
        )}
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            AreaChart(
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                data = { data },
                emptyContent = emptyPlaceholder(custom = state.customEmpty, onAction = { state.forceEmpty = false }),
                color = ChartyColor.Solid(state.color),
                lineConfig =
                    LineChartConfig(
                        lineWidth = state.lineWidth,
                        showPoints = state.showPoints,
                        pointRadius = state.pointRadius,
                        interpolation = state.interpolation,
                        fillAlpha = state.fillAlpha,
                        animation = playgroundAnimation(animate = animate),
                        animateValueChanges = state.animateValueChanges,
                        referenceLine = demoReferenceLine(enabled = state.referenceLine, value = state.referenceValue),
                        markers = demoMarkers(enabled = state.markers, size = series.size, label = "Peak"),
                        visibleWindow = windowOf(enabled = state.window, size = state.windowSize),
                    ),
            )
        },
        controls = { AreaControls(state = state) },
    )
}

@Composable
private fun AreaControls(state: AreaState) {
    ControlSection(title = "Data")
    IntSliderRow(label = "Points", value = state.points, valueRange = 3..60, onValueChange = { state.points = it })
    PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { state.tick++ })
    EmptyStateControls(
        forceEmpty = state.forceEmpty,
        onForceEmptyChange = { state.forceEmpty = it },
        custom = state.customEmpty,
        onCustomChange = { state.customEmpty = it },
    )
    ControlSection(title = "Area")
    SliderRow(
        label = "Line width",
        value = state.lineWidth,
        valueRange = 1f..8f,
        onValueChange = { state.lineWidth = it },
    )
    SliderRow(
        label = "Fill alpha",
        value = state.fillAlpha,
        valueRange = 0f..1f,
        onValueChange = { state.fillAlpha = it },
        decimals = 2,
    )
    ChoiceRow(
        label = "Interpolation",
        options = LineInterpolation.entries.toList(),
        selected = state.interpolation,
        labelOf = { it.name },
        onSelect = { state.interpolation = it },
    )
    SwitchRow(label = "Show points", checked = state.showPoints, onCheckedChange = { state.showPoints = it })
    if (state.showPoints) {
        SliderRow(
            label = "Point radius",
            value = state.pointRadius,
            valueRange = 2f..14f,
            onValueChange = { state.pointRadius = it },
        )
    }
    ControlSection(title = "Annotations")
    ReferenceLineControls(
        enabled = state.referenceLine,
        onEnabledChange = { state.referenceLine = it },
        value = state.referenceValue,
        onValueChange = { state.referenceValue = it },
        valueRange = 0f..100f,
    )
    SwitchRow(label = "Persistent marker", checked = state.markers, onCheckedChange = { state.markers = it })
    ControlSection(title = "Behaviour")
    SwitchRow(
        label = "Animate value changes",
        checked = state.animateValueChanges,
        onCheckedChange = { state.animateValueChanges = it },
    )
    VisibleWindowControls(
        enabled = state.window,
        onEnabledChange = { state.window = it },
        size = state.windowSize,
        onSizeChange = { state.windowSize = it },
        sizeRange = 2..30,
    )
    ControlSection(title = "Color")
    ColorRow(label = "Fill color", selected = state.color, onSelect = { state.color = it })
}

private class BarState {
    var bars by mutableStateOf(8)
    var tick by mutableStateOf(0)
    var negatives by mutableStateOf(false)
    var barWidthFraction by mutableStateOf(0.6f)
    var barSpacing by mutableStateOf(0f)
    var corner by mutableStateOf<CornerRadius>(CornerRadius.Medium)
    var color by mutableStateOf(playgroundPalette[3])
    var showDataLabels by mutableStateOf(false)
    var animateValueChanges by mutableStateOf(false)
    var negativeMode by mutableStateOf(NegativeValuesDrawMode.BELOW_AXIS)
    var referenceLine by mutableStateOf(false)
    var referenceValue by mutableStateOf(60f)
    var referenceBand by mutableStateOf(false)
    var bandLow by mutableStateOf(30f)
    var bandHigh by mutableStateOf(70f)
    var markers by mutableStateOf(false)
    var crosshair by mutableStateOf(false)
    var crosshairHorizontal by mutableStateOf(true)
    var crosshairDot by mutableStateOf(8f)
    var tooltipPosition by mutableStateOf(TooltipPosition.AUTO)
    var window by mutableStateOf(false)
    var windowSize by mutableStateOf(6)
    var forceEmpty by mutableStateOf(false)
    var customEmpty by mutableStateOf(true)
}

@Composable
internal fun BarPlayground() {
    val state = remember { BarState() }
    val animate = LocalPlaygroundAnimate.current
    val series =
        remember(state.bars, state.tick, state.negatives) {
            signedValues(count = state.bars, tick = state.tick, negatives = state.negatives)
                .mapIndexed { i, v -> BarData(label = "B${i + 1}", value = v) }
        }
    val data =
        if (state.forceEmpty) {
            emptyList()
        } else {
            series
        }

    val code =
        """
        val data = listOf(${series.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> BarData("B${'$'}{i + 1}", v) }

        BarChart(
            data = { data },${emptyContentArg(custom = state.customEmpty)}
            color = ChartyColor.Solid(Color(${colorHex(state.color)})),${codeArg(
            include = state.crosshair,
            text = "crosshair = ChartCrosshair(config = ChartCrosshairConfig(showHorizontalLine = ${state.crosshairHorizontal}, dotRadius = ${fc(
                state.crosshairDot,
            )})),",
        )}
            barConfig = BarChartConfig(
                barWidthFraction = ${fc(state.barWidthFraction)},
                barSpacing = ${fc(state.barSpacing)},
                cornerRadius = CornerRadius.${cornerName(state.corner)},${codeArg(
            include = state.negatives,
            text = "negativeValuesDrawMode = NegativeValuesDrawMode.${state.negativeMode.name},",
            indent = 16,
        )}${codeArg(
            include = state.showDataLabels,
            text = "showDataLabels = true,",
            indent = 16,
        )}${codeArg(
            include = state.animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = state.referenceLine,
            text = "referenceLine = ReferenceLineConfig(value = ${fc(state.referenceValue)}, label = \"Target\"),",
            indent = 16,
        )}${codeArg(
            include = state.referenceBand,
            text = "referenceBand = ReferenceBandConfig(lowValue = ${fc(
                state.bandLow,
            )}, highValue = ${fc(state.bandHigh)}, label = \"Healthy\"),",
            indent = 16,
        )}${codeArg(
            include = state.markers,
            text = "markers = listOf(PersistentMarker(dataIndex = ${series.size / 2}, label = \"Peak\", showGuideLine = true)),",
            indent = 16,
        )}${codeArg(
            include = state.tooltipPosition != TooltipPosition.AUTO,
            text = "tooltipPosition = TooltipPosition.${state.tooltipPosition.name},",
            indent = 16,
        )}${codeArg(
            include = state.window,
            text = "visibleWindow = ${state.windowSize},",
            indent = 16,
        )}
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            BarChart(
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                data = { data },
                emptyContent = emptyPlaceholder(custom = state.customEmpty, onAction = { state.forceEmpty = false }),
                color = ChartyColor.Solid(state.color),
                crosshair =
                    demoCrosshair(
                        enabled = state.crosshair,
                        showHorizontalLine = state.crosshairHorizontal,
                        dotRadius = state.crosshairDot,
                    )?.let { ChartCrosshair(config = it) },
                barConfig =
                    BarChartConfig(
                        barWidthFraction = state.barWidthFraction,
                        barSpacing = state.barSpacing,
                        cornerRadius = state.corner,
                        negativeValuesDrawMode = state.negativeMode,
                        animation = playgroundAnimation(animate = animate),
                        animateValueChanges = state.animateValueChanges,
                        referenceLine = demoReferenceLine(enabled = state.referenceLine, value = state.referenceValue),
                        referenceBand =
                            demoReferenceBand(
                                enabled = state.referenceBand,
                                low = state.bandLow,
                                high = state.bandHigh,
                            ),
                        markers = demoMarkers(enabled = state.markers, size = series.size, label = "Peak"),
                        tooltipPosition = state.tooltipPosition,
                        showDataLabels = state.showDataLabels,
                        visibleWindow = windowOf(enabled = state.window, size = state.windowSize),
                    ),
            )
        },
        controls = { BarControls(state = state) },
    )
}

@Composable
private fun BarControls(state: BarState) {
    ControlSection(title = "Data")
    IntSliderRow(label = "Bars", value = state.bars, valueRange = 2..20, onValueChange = { state.bars = it })
    SwitchRow(label = "Include negatives", checked = state.negatives, onCheckedChange = { state.negatives = it })
    if (state.negatives) {
        ChoiceRow(
            label = "Negative values",
            options = NegativeValuesDrawMode.entries.toList(),
            selected = state.negativeMode,
            labelOf = { it.name },
            onSelect = { state.negativeMode = it },
        )
    }
    PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { state.tick++ })
    EmptyStateControls(
        forceEmpty = state.forceEmpty,
        onForceEmptyChange = { state.forceEmpty = it },
        custom = state.customEmpty,
        onCustomChange = { state.customEmpty = it },
    )
    ControlSection(title = "Bars")
    SliderRow(
        label = "Width fraction",
        value = state.barWidthFraction,
        valueRange = 0.1f..1f,
        onValueChange = { state.barWidthFraction = it },
        decimals = 2,
    )
    SliderRow(
        label = "Bar spacing",
        value = state.barSpacing,
        valueRange = 0f..24f,
        onValueChange = { state.barSpacing = it },
        decimals = 0,
    )
    CornerRadiusControls(
        label = "Corner radius",
        corner = state.corner,
        onCornerChange = { state.corner = it },
    )
    SwitchRow(label = "Value labels", checked = state.showDataLabels, onCheckedChange = { state.showDataLabels = it })
    ControlSection(title = "Annotations")
    ReferenceLineControls(
        enabled = state.referenceLine,
        onEnabledChange = { state.referenceLine = it },
        value = state.referenceValue,
        onValueChange = { state.referenceValue = it },
        valueRange = 0f..100f,
    )
    ReferenceBandControls(
        enabled = state.referenceBand,
        onEnabledChange = { state.referenceBand = it },
        low = state.bandLow,
        onLowChange = { state.bandLow = it },
        high = state.bandHigh,
        onHighChange = { state.bandHigh = it },
        valueRange = 0f..100f,
    )
    SwitchRow(label = "Persistent marker", checked = state.markers, onCheckedChange = { state.markers = it })
    ControlSection(title = "Interaction")
    SwitchRow(label = "Crosshair", checked = state.crosshair, onCheckedChange = { state.crosshair = it })
    if (state.crosshair) {
        SwitchRow(
            label = "Horizontal line",
            checked = state.crosshairHorizontal,
            onCheckedChange = { state.crosshairHorizontal = it },
        )
        SliderRow(
            label = "Crosshair dot",
            value = state.crosshairDot,
            valueRange = 0f..16f,
            onValueChange = { state.crosshairDot = it },
            decimals = 0,
        )
    }
    ChoiceRow(
        label = "Tooltip position",
        options = TooltipPosition.entries.toList(),
        selected = state.tooltipPosition,
        labelOf = { it.name },
        onSelect = { state.tooltipPosition = it },
    )
    ControlSection(title = "Behaviour")
    SwitchRow(
        label = "Animate value changes",
        checked = state.animateValueChanges,
        onCheckedChange = { state.animateValueChanges = it },
    )
    VisibleWindowControls(
        enabled = state.window,
        onEnabledChange = { state.window = it },
        size = state.windowSize,
        onSizeChange = { state.windowSize = it },
        sizeRange = 2..20,
    )
    ControlSection(title = "Color")
    ColorRow(label = "Bar color", selected = state.color, onSelect = { state.color = it })
}

private class PointState {
    var points by mutableStateOf(14)
    var tick by mutableStateOf(0)
    var pointRadius by mutableStateOf(8f)
    var pointAlpha by mutableStateOf(1f)
    var color by mutableStateOf(playgroundPalette[4])
    var animateValueChanges by mutableStateOf(false)
    var highlightSelectedColumn by mutableStateOf(false)
    var referenceLine by mutableStateOf(false)
    var referenceValue by mutableStateOf(60f)
    var referenceBand by mutableStateOf(false)
    var bandLow by mutableStateOf(30f)
    var bandHigh by mutableStateOf(70f)
    var markers by mutableStateOf(false)
    var tooltipPosition by mutableStateOf(TooltipPosition.AUTO)
    var downsample by mutableStateOf(false)
    var downsampleThreshold by mutableStateOf(20)
    var window by mutableStateOf(false)
    var windowSize by mutableStateOf(8)
    var forceEmpty by mutableStateOf(false)
    var customEmpty by mutableStateOf(true)
}

@Composable
internal fun PointPlayground() {
    val state = remember { PointState() }
    val animate = LocalPlaygroundAnimate.current
    val series =
        remember(state.points, state.tick) {
            randomValues(count = state.points, tick = state.tick)
                .mapIndexed { i, v -> PointData(label = "P${i + 1}", value = v) }
        }
    val data =
        if (state.forceEmpty) {
            emptyList()
        } else {
            series
        }

    val code =
        """
        val data = listOf(${series.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> PointData("P${'$'}{i + 1}", v) }

        PointChart(
            data = { data },${emptyContentArg(custom = state.customEmpty)}
            color = ChartyColor.Solid(Color(${colorHex(state.color)})),
            pointConfig = PointChartConfig(
                pointRadius = ${fc(state.pointRadius)},
                pointAlpha = ${fc(state.pointAlpha)},${codeArg(
            include = state.animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = state.highlightSelectedColumn,
            text = "highlightSelectedColumn = true,",
            indent = 16,
        )}${codeArg(
            include = state.referenceLine,
            text = "referenceLine = ReferenceLineConfig(value = ${fc(state.referenceValue)}, label = \"Target\"),",
            indent = 16,
        )}${codeArg(
            include = state.referenceBand,
            text = "referenceBand = ReferenceBandConfig(lowValue = ${fc(
                state.bandLow,
            )}, highValue = ${fc(state.bandHigh)}, label = \"Healthy\"),",
            indent = 16,
        )}${codeArg(
            include = state.markers,
            text = "markers = listOf(PersistentMarker(dataIndex = ${series.size / 2}, label = \"Peak\", showGuideLine = true)),",
            indent = 16,
        )}${codeArg(
            include = state.tooltipPosition != TooltipPosition.AUTO,
            text = "tooltipPosition = TooltipPosition.${state.tooltipPosition.name},",
            indent = 16,
        )}${codeArg(
            include = state.downsample,
            text = "downsampleThreshold = ${state.downsampleThreshold},",
            indent = 16,
        )}${codeArg(
            include = state.window,
            text = "visibleWindow = ${state.windowSize},",
            indent = 16,
        )}
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            PointChart(
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                data = { data },
                emptyContent = emptyPlaceholder(custom = state.customEmpty, onAction = { state.forceEmpty = false }),
                color = ChartyColor.Solid(state.color),
                pointConfig =
                    PointChartConfig(
                        pointRadius = state.pointRadius,
                        pointAlpha = state.pointAlpha,
                        animation = playgroundAnimation(animate = animate),
                        animateValueChanges = state.animateValueChanges,
                        referenceLine = demoReferenceLine(enabled = state.referenceLine, value = state.referenceValue),
                        referenceBand =
                            demoReferenceBand(
                                enabled = state.referenceBand,
                                low = state.bandLow,
                                high = state.bandHigh,
                            ),
                        markers = demoMarkers(enabled = state.markers, size = series.size, label = "Peak"),
                        tooltipPosition = state.tooltipPosition,
                        highlightSelectedColumn = state.highlightSelectedColumn,
                        downsampleThreshold =
                            thresholdOf(
                                enabled = state.downsample,
                                value = state.downsampleThreshold,
                            ),
                        visibleWindow = windowOf(enabled = state.window, size = state.windowSize),
                    ),
            )
        },
        controls = { PointControls(state = state) },
    )
}

@Composable
private fun PointControls(state: PointState) {
    ControlSection(title = "Data")
    IntSliderRow(label = "Points", value = state.points, valueRange = 3..40, onValueChange = { state.points = it })
    PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { state.tick++ })
    EmptyStateControls(
        forceEmpty = state.forceEmpty,
        onForceEmptyChange = { state.forceEmpty = it },
        custom = state.customEmpty,
        onCustomChange = { state.customEmpty = it },
    )
    ControlSection(title = "Points")
    SliderRow(
        label = "Radius",
        value = state.pointRadius,
        valueRange = 2f..20f,
        onValueChange = { state.pointRadius = it },
    )
    SliderRow(
        label = "Opacity",
        value = state.pointAlpha,
        valueRange = 0.2f..1f,
        onValueChange = { state.pointAlpha = it },
        decimals = 2,
    )
    ControlSection(title = "Annotations")
    ReferenceLineControls(
        enabled = state.referenceLine,
        onEnabledChange = { state.referenceLine = it },
        value = state.referenceValue,
        onValueChange = { state.referenceValue = it },
        valueRange = 0f..100f,
    )
    ReferenceBandControls(
        enabled = state.referenceBand,
        onEnabledChange = { state.referenceBand = it },
        low = state.bandLow,
        onLowChange = { state.bandLow = it },
        high = state.bandHigh,
        onHighChange = { state.bandHigh = it },
        valueRange = 0f..100f,
    )
    SwitchRow(label = "Persistent marker", checked = state.markers, onCheckedChange = { state.markers = it })
    ControlSection(title = "Interaction")
    SwitchRow(
        label = "Highlight column",
        checked = state.highlightSelectedColumn,
        onCheckedChange = { state.highlightSelectedColumn = it },
    )
    ChoiceRow(
        label = "Tooltip position",
        options = TooltipPosition.entries.toList(),
        selected = state.tooltipPosition,
        labelOf = { it.name },
        onSelect = { state.tooltipPosition = it },
    )
    ControlSection(title = "Performance")
    SwitchRow(
        label = "Animate value changes",
        checked = state.animateValueChanges,
        onCheckedChange = { state.animateValueChanges = it },
    )
    SwitchRow(label = "Downsample", checked = state.downsample, onCheckedChange = { state.downsample = it })
    if (state.downsample) {
        IntSliderRow(
            label = "Threshold",
            value = state.downsampleThreshold,
            valueRange = 3..40,
            onValueChange = { state.downsampleThreshold = it },
        )
    }
    VisibleWindowControls(
        enabled = state.window,
        onEnabledChange = { state.window = it },
        size = state.windowSize,
        onSizeChange = { state.windowSize = it },
        sizeRange = 2..30,
    )
    ControlSection(title = "Color")
    ColorRow(label = "Point color", selected = state.color, onSelect = { state.color = it })
}

private class BubbleState {
    var points by mutableStateOf(8)
    var tick by mutableStateOf(0)
    var maxRadius by mutableStateOf(50f)
    var minRadius by mutableStateOf(12f)
    var pointAlpha by mutableStateOf(1f)
    var color by mutableStateOf(playgroundPalette[1])
    var animateValueChanges by mutableStateOf(false)
    var referenceLine by mutableStateOf(false)
    var referenceValue by mutableStateOf(60f)
    var crosshair by mutableStateOf(false)
}

@Composable
internal fun BubblePlayground() {
    val state = remember { BubbleState() }
    val animate = LocalPlaygroundAnimate.current
    val data =
        remember(state.points, state.tick) {
            val ys = randomValues(count = state.points, tick = state.tick, min = 20f, max = 90f)
            val sizes = randomValues(count = state.points, tick = state.tick + 7, min = 60f, max = 300f)
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
            color = ChartyColor.Solid(Color(${colorHex(state.color)})),${codeArg(
            include = state.crosshair,
            text = "crosshair = ChartCrosshair(),",
        )}
            config = PointChartConfig(
                pointRadius = ${fc(state.maxRadius)},
                pointAlpha = ${fc(state.pointAlpha)},${codeArg(
            include = state.animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = state.referenceLine,
            text = "referenceLine = ReferenceLineConfig(value = ${fc(state.referenceValue)}, label = \"Target\"),",
            indent = 16,
        )}
            ),
            minBubbleRadius = ${fc(state.minRadius)},
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            BubbleChart(
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                data = { data },
                color = ChartyColor.Solid(state.color),
                config =
                    PointChartConfig(
                        pointRadius = state.maxRadius,
                        pointAlpha = state.pointAlpha,
                        animation = playgroundAnimation(animate = animate),
                        animateValueChanges = state.animateValueChanges,
                        referenceLine = demoReferenceLine(enabled = state.referenceLine, value = state.referenceValue),
                    ),
                minBubbleRadius = state.minRadius,
                crosshair =
                    demoCrosshair(enabled = state.crosshair, showHorizontalLine = true, dotRadius = 8f)
                        ?.let { ChartCrosshair(config = it) },
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(
                label = "Bubbles",
                value = state.points,
                valueRange = 3..15,
                onValueChange = { state.points = it },
            )
            PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { state.tick++ })
            ControlSection(title = "Size range")
            SliderRow(
                label = "Max radius",
                value = state.maxRadius,
                valueRange = 30f..80f,
                onValueChange = { state.maxRadius = it },
                decimals = 0,
            )
            SliderRow(
                label = "Min radius",
                value = state.minRadius,
                valueRange = 5f..25f,
                onValueChange = { state.minRadius = it },
                decimals = 0,
            )
            SliderRow(
                label = "Opacity",
                value = state.pointAlpha,
                valueRange = 0.2f..1f,
                onValueChange = { state.pointAlpha = it },
                decimals = 2,
            )
            ControlSection(title = "Annotations")
            ReferenceLineControls(
                enabled = state.referenceLine,
                onEnabledChange = { state.referenceLine = it },
                value = state.referenceValue,
                onValueChange = { state.referenceValue = it },
                valueRange = 0f..100f,
            )
            ControlSection(title = "Behaviour")
            SwitchRow(label = "Crosshair", checked = state.crosshair, onCheckedChange = { state.crosshair = it })
            SwitchRow(
                label = "Animate value changes",
                checked = state.animateValueChanges,
                onCheckedChange = { state.animateValueChanges = it },
            )
            ControlSection(title = "Color")
            ColorRow(label = "Bubble color", selected = state.color, onSelect = { state.color = it })
        },
    )
}

private class PieState {
    var slices by mutableStateOf(5)
    var tick by mutableStateOf(0)
    var style by mutableStateOf(PieChartStyle.PIE)
    var donutHoleRatio by mutableStateOf(0.5f)
    var sliceSpacing by mutableStateOf(0f)
    var startAngle by mutableStateOf(-90f)
    var showLabels by mutableStateOf(true)
    var showPercentage by mutableStateOf(true)
    var showValue by mutableStateOf(false)
    var labelsOutside by mutableStateOf(false)
    var showCenterText by mutableStateOf(false)
    var interactionEnabled by mutableStateOf(true)
    var selectedScale by mutableStateOf(1.1f)
    var pullOut by mutableStateOf(8f)
    var unselectedOpacity by mutableStateOf(0.6f)
    var forceEmpty by mutableStateOf(false)
    var customEmpty by mutableStateOf(true)
}

@Composable
internal fun PiePlayground() {
    val state = remember { PieState() }
    val animate = LocalPlaygroundAnimate.current
    val series =
        remember(state.slices, state.tick) {
            randomValues(count = state.slices, tick = state.tick, min = 10f, max = 100f).mapIndexed { i, v ->
                PieData(
                    label = "S${i + 1}",
                    value = v,
                    color = ChartyColor.Solid(playgroundPalette[i % playgroundPalette.size]),
                )
            }
        }
    val data =
        if (state.forceEmpty) {
            emptyList()
        } else {
            series
        }

    val code =
        """
        val data = listOf(${series.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> PieData("S${'$'}{i + 1}", v) }

        PieChart(
            data = { data },${emptyContentArg(custom = state.customEmpty)}
            config = PieChartConfig(
                style = PieChartStyle.${state.style.name},
                donutHoleRatio = ${fc(state.donutHoleRatio)},
                startAngleDegrees = ${fc(state.startAngle)},
                sliceSpacingDegrees = ${fc(state.sliceSpacing)},
                shouldShowCenterText = ${state.showCenterText},
                labelConfig = LabelConfig(
                    shouldShowLabels = ${state.showLabels},
                    shouldShowPercentage = ${state.showPercentage},
                    shouldShowValue = ${state.showValue},
                    shouldShowLabelsOutside = ${state.labelsOutside},
                ),
                interactionConfig = InteractionConfig(
                    isEnabled = ${state.interactionEnabled},
                    selectedScaleMultiplier = ${fc(state.selectedScale)},
                    selectedSlicePullOutDistance = ${fc(state.pullOut)},
                    unselectedSliceOpacity = ${fc(state.unselectedOpacity)},
                ),
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        cartesian = false,
        code = code,
        chart = {
            PieChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                emptyContent = emptyPlaceholder(custom = state.customEmpty, onAction = { state.forceEmpty = false }),
                config =
                    PieChartConfig(
                        style = state.style,
                        donutHoleRatio = state.donutHoleRatio,
                        startAngleDegrees = state.startAngle,
                        sliceSpacingDegrees = state.sliceSpacing,
                        shouldShowCenterText = state.showCenterText,
                        labelConfig =
                            LabelConfig(
                                shouldShowLabels = state.showLabels,
                                shouldShowPercentage = state.showPercentage,
                                shouldShowValue = state.showValue,
                                shouldShowLabelsOutside = state.labelsOutside,
                            ),
                        interactionConfig =
                            InteractionConfig(
                                isEnabled = state.interactionEnabled,
                                selectedScaleMultiplier = state.selectedScale,
                                selectedSlicePullOutDistance = state.pullOut,
                                unselectedSliceOpacity = state.unselectedOpacity,
                            ),
                        animation = playgroundAnimation(animate = animate),
                    ),
            )
        },
        controls = { PieControls(state = state) },
    )
}

@Composable
private fun PieControls(state: PieState) {
    ControlSection(title = "Data")
    IntSliderRow(label = "Slices", value = state.slices, valueRange = 2..8, onValueChange = { state.slices = it })
    PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { state.tick++ })
    EmptyStateControls(
        forceEmpty = state.forceEmpty,
        onForceEmptyChange = { state.forceEmpty = it },
        custom = state.customEmpty,
        onCustomChange = { state.customEmpty = it },
    )
    ControlSection(title = "Shape")
    ChoiceRow(
        label = "Style",
        options = PieChartStyle.entries.toList(),
        selected = state.style,
        labelOf = { it.name },
        onSelect = { state.style = it },
    )
    if (state.style == PieChartStyle.DONUT) {
        SliderRow(
            label = "Hole ratio",
            value = state.donutHoleRatio,
            valueRange = 0f..0.9f,
            onValueChange = { state.donutHoleRatio = it },
            decimals = 2,
        )
        SwitchRow(
            label = "Center text",
            checked = state.showCenterText,
            onCheckedChange = { state.showCenterText = it },
        )
    }
    SliderRow(
        label = "Slice spacing",
        value = state.sliceSpacing,
        valueRange = 0f..10f,
        onValueChange = { state.sliceSpacing = it },
    )
    SliderRow(
        label = "Start angle",
        value = state.startAngle,
        valueRange = -180f..180f,
        onValueChange = { state.startAngle = it },
        decimals = 0,
    )
    ControlSection(title = "Labels")
    SwitchRow(label = "Show labels", checked = state.showLabels, onCheckedChange = { state.showLabels = it })
    SwitchRow(
        label = "Show percentage",
        checked = state.showPercentage,
        onCheckedChange = { state.showPercentage = it },
    )
    SwitchRow(label = "Show value", checked = state.showValue, onCheckedChange = { state.showValue = it })
    SwitchRow(label = "Labels outside", checked = state.labelsOutside, onCheckedChange = { state.labelsOutside = it })
    ControlSection(title = "Selection")
    SwitchRow(
        label = "Selection enabled",
        checked = state.interactionEnabled,
        onCheckedChange = { state.interactionEnabled = it },
    )
    if (state.interactionEnabled) {
        SliderRow(
            label = "Selected scale",
            value = state.selectedScale,
            valueRange = 1f..1.5f,
            onValueChange = { state.selectedScale = it },
            decimals = 2,
        )
        SliderRow(
            label = "Pull-out distance",
            value = state.pullOut,
            valueRange = 0f..30f,
            onValueChange = { state.pullOut = it },
            decimals = 0,
        )
        SliderRow(
            label = "Unselected opacity",
            value = state.unselectedOpacity,
            valueRange = 0f..1f,
            onValueChange = { state.unselectedOpacity = it },
            decimals = 2,
        )
    }
}

private class ComboState {
    var points by mutableStateOf(8)
    var tick by mutableStateOf(0)
    var barWidthFraction by mutableStateOf(0.6f)
    var corner by mutableStateOf<CornerRadius>(CornerRadius.Medium)
    var lineWidth by mutableStateOf(3f)
    var showPoints by mutableStateOf(true)
    var pointRadius by mutableStateOf(6f)
    var smoothCurve by mutableStateOf(false)
    var secondaryAxis by mutableStateOf(false)
    var barColor by mutableStateOf(playgroundPalette[0])
    var lineColor by mutableStateOf(playgroundPalette[3])
    var animateValueChanges by mutableStateOf(false)
    var referenceLine by mutableStateOf(false)
    var referenceValue by mutableStateOf(120f)
    var markers by mutableStateOf(false)
    var window by mutableStateOf(false)
    var windowSize by mutableStateOf(6)
}

@Composable
internal fun ComboPlayground() {
    val state = remember { ComboState() }
    val animate = LocalPlaygroundAnimate.current
    val data =
        remember(state.points, state.tick) {
            val bars = randomValues(count = state.points, tick = state.tick, min = 40f, max = 200f)
            val lines = randomValues(count = state.points, tick = state.tick + 7, min = 40f, max = 200f)
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
            barColor = ChartyColor.Solid(Color(${colorHex(state.barColor)})),
            lineColor = ChartyColor.Solid(Color(${colorHex(state.lineColor)})),
            comboConfig = ComboChartConfig(
                barWidthFraction = ${fc(state.barWidthFraction)},
                barCornerRadius = CornerRadius.${cornerName(state.corner)},
                lineWidth = ${fc(state.lineWidth)},
                showPoints = ${state.showPoints},
                pointRadius = ${fc(state.pointRadius)},
                smoothCurve = ${state.smoothCurve},
                secondaryAxisForLine = ${state.secondaryAxis},${codeArg(
            include = state.animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = state.referenceLine,
            text = "referenceLine = ReferenceLineConfig(value = ${fc(state.referenceValue)}, label = \"Target\"),",
            indent = 16,
        )}${codeArg(
            include = state.markers,
            text = "markers = listOf(PersistentMarker(dataIndex = ${data.size / 2}, label = \"Peak\", showGuideLine = true)),",
            indent = 16,
        )}${codeArg(
            include = state.window,
            text = "visibleWindow = ${state.windowSize},",
            indent = 16,
        )}
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            ComboChart(
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                data = { data },
                barColor = ChartyColor.Solid(state.barColor),
                lineColor = ChartyColor.Solid(state.lineColor),
                comboConfig =
                    ComboChartConfig(
                        barWidthFraction = state.barWidthFraction,
                        barCornerRadius = state.corner,
                        lineWidth = state.lineWidth,
                        showPoints = state.showPoints,
                        pointRadius = state.pointRadius,
                        smoothCurve = state.smoothCurve,
                        secondaryAxisForLine = state.secondaryAxis,
                        animation = playgroundAnimation(animate = animate),
                        animateValueChanges = state.animateValueChanges,
                        referenceLine = demoReferenceLine(enabled = state.referenceLine, value = state.referenceValue),
                        markers = demoMarkers(enabled = state.markers, size = data.size, label = "Peak"),
                        visibleWindow = windowOf(enabled = state.window, size = state.windowSize),
                    ),
            )
        },
        controls = { ComboControls(state = state) },
    )
}

@Composable
private fun ComboControls(state: ComboState) {
    ControlSection(title = "Data")
    IntSliderRow(label = "Groups", value = state.points, valueRange = 3..14, onValueChange = { state.points = it })
    PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { state.tick++ })
    ControlSection(title = "Bars & line")
    SliderRow(
        label = "Bar width fraction",
        value = state.barWidthFraction,
        valueRange = 0.2f..1f,
        onValueChange = { state.barWidthFraction = it },
        decimals = 2,
    )
    CornerRadiusControls(
        label = "Bar corner radius",
        corner = state.corner,
        onCornerChange = { state.corner = it },
    )
    SliderRow(
        label = "Line width",
        value = state.lineWidth,
        valueRange = 1f..6f,
        onValueChange = { state.lineWidth = it },
    )
    SwitchRow(label = "Show points", checked = state.showPoints, onCheckedChange = { state.showPoints = it })
    if (state.showPoints) {
        SliderRow(
            label = "Point radius",
            value = state.pointRadius,
            valueRange = 2f..14f,
            onValueChange = { state.pointRadius = it },
        )
    }
    SwitchRow(label = "Smooth curve", checked = state.smoothCurve, onCheckedChange = { state.smoothCurve = it })
    SwitchRow(
        label = "Secondary axis for line",
        checked = state.secondaryAxis,
        onCheckedChange = { state.secondaryAxis = it },
    )
    ControlSection(title = "Annotations")
    ReferenceLineControls(
        enabled = state.referenceLine,
        onEnabledChange = { state.referenceLine = it },
        value = state.referenceValue,
        onValueChange = { state.referenceValue = it },
        valueRange = 0f..200f,
    )
    SwitchRow(label = "Persistent marker", checked = state.markers, onCheckedChange = { state.markers = it })
    ControlSection(title = "Behaviour")
    SwitchRow(
        label = "Animate value changes",
        checked = state.animateValueChanges,
        onCheckedChange = { state.animateValueChanges = it },
    )
    VisibleWindowControls(
        enabled = state.window,
        onEnabledChange = { state.window = it },
        size = state.windowSize,
        onSizeChange = { state.windowSize = it },
        sizeRange = 2..14,
    )
    ControlSection(title = "Colors")
    ColorRow(label = "Bar color", selected = state.barColor, onSelect = { state.barColor = it })
    ColorRow(label = "Line color", selected = state.lineColor, onSelect = { state.lineColor = it })
}
