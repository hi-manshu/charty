package com.himanshoe.charty.line

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartLegend
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateLineGroupChartDescription
import com.himanshoe.charty.common.animation.isAnimated
import com.himanshoe.charty.common.animation.toFloatSpec
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint
import com.himanshoe.charty.line.ext.getAllValues
import com.himanshoe.charty.line.ext.getLabels
import com.himanshoe.charty.line.internal.multiline.calculateSeriesPointPositions
import com.himanshoe.charty.line.internal.multiline.drawLineSeries
import com.himanshoe.charty.line.internal.multiline.drawMultilineChartCrosshair
import com.himanshoe.charty.line.internal.multiline.multilineChartClickHandler

private data class MultilineDrawParams(
    val dataList: List<LineGroup>,
    val chartContext: ChartContext,
    val colorList: List<Color>,
    val lineConfig: LineChartConfig,
    val animationProgress: Float,
    val pointBounds: MutableList<Pair<Offset, MultilinePoint>>,
    val crosshairBounds: MutableList<Pair<Offset, MultilinePoint>>,
    val onPointClick: ((MultilinePoint) -> Unit)?,
    val crosshairManager: CrosshairManager<MultilinePoint>?,
    val crosshairState: CrosshairState?,
    val tooltipState: TooltipState?,
    val textMeasurer: TextMeasurer,
    val interactionConfig: ChartInteractionConfig,
)

/**
 * A composable function that displays a multiline chart.
 *
 * Example:
 * ```kotlin
 * MultilineChart(
 *     data = {
 *         listOf(
 *             LineGroup("Jan", listOf(20f, 12f)),
 *             LineGroup("Feb", listOf(45f, 30f)),
 *             LineGroup("Mar", listOf(30f, 25f)),
 *         )
 *     },
 * )
 * ```
 *
 * @param data A lambda function that returns a list of [LineGroup].
 * @param modifier The modifier to be applied to the chart.
 * @param colors The color or color scheme for the lines.
 * @param lineConfig The configuration for the lines' appearance and behavior.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param onPointClick A lambda function invoked when a point is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun MultilineChart(
    data: () -> List<LineGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColors.DefaultMultiline,
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onPointClick: ((MultilinePoint) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Multiline chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val (minValue, maxValue, colorList) =
        remember(dataList, colors, lineConfig.negativeValuesDrawMode) {
            val allValues = dataList.getAllValues()
            Triple(calculateMinValue(allValues), calculateMaxValue(allValues), colors.value)
        }

    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = remember { Animatable(if (lineConfig.animation.isAnimated) 0f else 1f) }
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val pointBounds = remember { mutableListOf<Pair<Offset, MultilinePoint>>() }
    val crosshairBounds = remember { mutableListOf<Pair<Offset, MultilinePoint>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<MultilinePoint>(lineConfig.crosshairConfig != null)
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(lineConfig.animation) {
        if (lineConfig.animation.isAnimated) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = lineConfig.animation.toFloatSpec(),
            )
        }
    }

    val chartDescription =
        rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
            generateLineGroupChartDescription(it, "multiline")
        }

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    Column(modifier = modifier) {
        ChartScaffold(
            modifier =
                buildMultilineModifier(
                    base = Modifier.weight(1f),
                    crosshairManager = crosshairManager,
                    dataList = dataList,
                    lineConfig = lineConfig,
                    pointBounds = pointBounds,
                    crosshairBounds = crosshairBounds,
                    onPointClick = onPointClick,
                    onTooltipStateChange = { state, _ -> tooltipState = state },
                    interactionConfig = interactionConfig,
                ),
            xLabels = dataList.getLabels(),
            yAxisConfig =
                AxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    steps = 6,
                    drawAxisAtZero = isBelowAxisMode,
                ),
            config = scaffoldConfig,
            contentDescription = chartDescription,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig, chartContext)
            drawMultilineContent(
                MultilineDrawParams(
                    dataList = dataList,
                    chartContext = chartContext,
                    colorList = colorList,
                    lineConfig = lineConfig,
                    animationProgress = animationProgress.value,
                    pointBounds = pointBounds,
                    crosshairBounds = crosshairBounds,
                    onPointClick = onPointClick,
                    crosshairManager = crosshairManager,
                    crosshairState = animatedCrosshairState,
                    tooltipState = tooltipState,
                    textMeasurer = textMeasurer,
                    interactionConfig = interactionConfig,
                ),
            )
        }
        if (lineConfig.legendLabels.isNotEmpty()) {
            ChartLegend(
                labels = lineConfig.legendLabels,
                colors = colorList,
                textStyle = lineConfig.legendTextStyle,
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawMultilineContent(p: MultilineDrawParams) {
    p.pointBounds.clear()
    p.crosshairBounds.clear()
    val seriesCount =
        p.dataList
            .getOrNull(0)
            ?.values
            ?.size ?: 0

    if (p.crosshairManager != null) {
        p.chartContext.calculateSeriesPointPositions(p.dataList, 0).fastForEachIndexed { index, pos ->
            val group = p.dataList.getOrNull(index) ?: return@fastForEachIndexed
            p.crosshairBounds.add(pos to MultilinePoint(group, 0, index, group.values.getOrNull(0) ?: 0f))
        }
    }

    for (seriesIndex in 0 until seriesCount) {
        drawLineSeries(
            seriesIndex = seriesIndex,
            dataList = p.dataList,
            chartContext = p.chartContext,
            lineConfig = p.lineConfig,
            colorList = p.colorList,
            animationProgress = p.animationProgress,
            pointBounds = if (p.onPointClick != null) p.pointBounds else null,
        )
    }

    if (p.crosshairManager == null) {
        p.tooltipState?.let { state ->
            drawTooltip(
                tooltipState = state,
                config = p.lineConfig.tooltipConfig,
                textMeasurer = p.textMeasurer,
                chartWidth = p.chartContext.right,
                chartTop = p.chartContext.top,
                chartBottom = p.chartContext.bottom,
            )
        }
    }
    drawInteractionOverlays(p.interactionConfig, p.chartContext, p.dataList.size, p.textMeasurer)

    p.crosshairState?.let { state ->
        p.lineConfig.crosshairConfig?.let { config ->
            drawMultilineChartCrosshair(
                state = state,
                config = config,
                chartContext = p.chartContext,
                dataList = p.dataList,
                colorList = p.colorList,
                textMeasurer = p.textMeasurer,
            )
        }
    }
}

private fun buildMultilineModifier(
    base: Modifier,
    crosshairManager: CrosshairManager<MultilinePoint>?,
    dataList: List<LineGroup>,
    lineConfig: LineChartConfig,
    pointBounds: MutableList<Pair<Offset, MultilinePoint>>,
    crosshairBounds: MutableList<Pair<Offset, MultilinePoint>>,
    onPointClick: ((MultilinePoint) -> Unit)?,
    onTooltipStateChange: (TooltipState?, MultilinePoint?) -> Unit,
    interactionConfig: ChartInteractionConfig,
): Modifier {
    val mod: Modifier =
        when {
            crosshairManager != null ->
                Modifier.chartCrosshairHandler(
                    dataList = dataList,
                    pointBounds = crosshairBounds,
                    onCrosshairUpdate = crosshairManager::update,
                    labelFormatter = { point ->
                        point.lineGroup.values
                            .mapIndexed { i, v -> "L${i + 1}: $v" }
                            .joinToString("  ")
                    },
                    dismissOnRelease = lineConfig.crosshairConfig?.dismissOnRelease ?: true,
                )
            onPointClick != null ->
                Modifier.multilineChartClickHandler(
                    dataList = dataList,
                    lineConfig = lineConfig,
                    pointBounds = pointBounds,
                    onPointClick = onPointClick,
                    onTooltipStateChange = onTooltipStateChange,
                )
            else -> Modifier
        }
    return buildInteractionModifier(base = base.then(mod), interactionConfig = interactionConfig, dataList = dataList)
}
