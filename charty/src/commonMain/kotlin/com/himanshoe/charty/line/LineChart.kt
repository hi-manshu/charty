package com.himanshoe.charty.line

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateLineChartDescription
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.chartZoomAndPan
import com.himanshoe.charty.common.gesture.rememberCrosshairManager
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.internal.line.calculatePointPositions
import com.himanshoe.charty.line.internal.line.drawAnimatedPoints
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import com.himanshoe.charty.line.internal.line.drawLineChartTooltip
import com.himanshoe.charty.line.internal.line.drawSmoothLine
import com.himanshoe.charty.line.internal.line.drawStraightLineSegments
import com.himanshoe.charty.line.internal.line.lineChartInteractionHandler

/**
 * A composable function that displays an interactive line chart.
 *
 * @param data A lambda returning the list of [LineData] points to display.
 * @param modifier Modifier applied to the chart composable.
 * @param color The line and point colour defined by [ChartyColor].
 * @param lineConfig Appearance and behaviour configuration for the line.
 * @param scaffoldConfig Axes and label configuration for the chart scaffold.
 * @param onPointClick Invoked when the user taps a data point.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 *
 * Example:
 * ```kotlin
 * LineChart(
 *     data = { priceData },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 *     lineConfig = LineChartConfig(smoothCurve = true),
 * )
 * ```
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun LineChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onPointClick: ((LineData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Line chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val (minValue, maxValue) = remember(dataList, lineConfig.negativeValuesDrawMode) {
        val values = dataList.getValues()
        calculateMinValue(values) to calculateMaxValue(values)
    }
    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = rememberChartAnimation(lineConfig.animation)

    val tooltipManager = rememberTooltipManager<Offset, LineData>()
    val textMeasurer = rememberTextMeasurer()

    val crosshairManager = if (lineConfig.crosshairConfig != null) rememberCrosshairManager() else null

    val chartDescription = rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
        generateLineChartDescription(it, minValue, maxValue)
    }

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val interactionModifier = Modifier.lineChartInteractionHandler(
        dataList = dataList,
        lineConfig = lineConfig,
        pointBounds = tooltipManager.bounds,
        onPointClick = onPointClick,
        onTooltipStateChange = tooltipManager::updateTooltip,
        crosshairManager = crosshairManager,
        brushSelectionState = interactionConfig.brushSelectionState,
        onRangeSelect = interactionConfig.onRangeSelect,
    )

    val zoomModifier = interactionConfig.viewPortState?.let { Modifier.chartZoomAndPan(it) } ?: Modifier

    ChartScaffold(
        modifier = modifier.then(interactionModifier).then(zoomModifier),
        xLabels = dataList.getLabels(),
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = 6,
            drawAxisAtZero = isBelowAxisMode,
        ),
        config = scaffoldConfig,
        contentDescription = chartDescription,
    ) { chartContext ->
        updateInteractionBounds(interactionConfig, chartContext)

        tooltipManager.clearBounds()

        val pointPositions = chartContext.calculatePointPositions(dataList)

        if (onPointClick != null || crosshairManager != null) {
            pointPositions.fastForEachIndexed { index, position ->
                tooltipManager.bounds.add(position to dataList[index])
            }
        }

        drawLineContent(
            pointPositions = pointPositions,
            color = color,
            lineConfig = lineConfig,
            animationProgress = animationProgress.value,
        )

        lineConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer,
            )
        }

        drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)

        crosshairManager?.state?.let { crosshairState ->
            lineConfig.crosshairConfig?.let { crosshairConfig ->
                drawLineChartCrosshair(
                    state = crosshairState,
                    config = crosshairConfig,
                    chartContext = chartContext,
                    textMeasurer = textMeasurer,
                    chartColor = color,
                )
            }
        }

        if (crosshairManager == null) {
            tooltipManager.tooltipState?.let { state ->
                drawLineChartTooltip(
                    tooltipState = state,
                    pointBounds = tooltipManager.bounds,
                    color = color,
                    lineConfig = lineConfig,
                    chartContext = chartContext,
                    textMeasurer = textMeasurer,
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLineContent(
    pointPositions: List<Offset>,
    color: ChartyColor,
    lineConfig: LineChartConfig,
    animationProgress: Float,
) {
    if (lineConfig.smoothCurve) {
        drawSmoothLine(
            pointPositions = pointPositions,
            color = color,
            lineConfig = lineConfig,
            animationProgress = animationProgress,
        )
    } else {
        drawStraightLineSegments(
            pointPositions = pointPositions,
            color = color,
            lineConfig = lineConfig,
            animationProgress = animationProgress,
        )
    }

    if (lineConfig.showPoints) {
        drawAnimatedPoints(
            pointPositions = pointPositions,
            color = color,
            lineConfig = lineConfig,
            animationProgress = animationProgress,
        )
    }
}
