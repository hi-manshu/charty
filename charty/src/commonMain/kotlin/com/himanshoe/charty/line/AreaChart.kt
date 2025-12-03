package com.himanshoe.charty.line

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.ext.createAreaBrush
import com.himanshoe.charty.line.ext.createAreaPath
import com.himanshoe.charty.line.ext.createLineBrush
import com.himanshoe.charty.line.ext.createLinePath
import com.himanshoe.charty.line.internal.area.createAreaChartModifier

private const val DEFAULT_FILL_ALPHA = 0.3f
private const val DEFAULT_AXIS_STEPS = 6
private const val HIGHLIGHT_LINE_ALPHA = 0.1f
private const val HIGHLIGHT_LINE_WIDTH = 1.5f
private const val HIGHLIGHT_CIRCLE_OUTER_PADDING = 3f
private const val HIGHLIGHT_CIRCLE_INNER_PADDING = 2f

/**
 * Parameters for drawing area chart
 */
private data class AreaChartDrawParams(
    val dataList: List<LineData>,
    val pointPositions: List<Offset>,
    val baselineY: Float,
    val config: LineChartConfig,
    val color: ChartyColor,
    val fillAlpha: Float,
    val animationProgress: Float,
    val chartContext: com.himanshoe.charty.common.ChartContext,
    val onBarBoundCalculated: (Pair<Offset, LineData>) -> Unit,
)

/**
 * Area Chart - Line chart with filled area below the line
 *
 * An area chart is similar to a line chart but the area between the line and the axis
 * is filled with color/gradient. Useful for showing cumulative trends and emphasizing
 * the magnitude of change over time.
 *
 * Usage:
 * ```kotlin
 * AreaChart(
 *     data = {
 *         listOf(
 *             LineData("Jan", 20f),
 *             LineData("Feb", 45f),
 *             LineData("Mar", 30f),
 *             LineData("Apr", 70f)
 *         )
 *     },
 *     color = ChartyColor.Gradient(
 *         listOf(Color(0xFF2196F3), Color(0xFF2196F3).copy(alpha = 0.3f))
 *     ),
 *     lineConfig = LineChartConfig(
 *         lineWidth = 3f,
 *         showPoints = true
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of line data points to display
 * @param modifier Modifier for the chart
 * @param color Color configuration for area fill (gradient recommended for fade effect)
 * @param lineConfig Configuration for line and points appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param fillAlpha Alpha transparency for the filled area (0.0f - 1.0f)
 * @param onPointClick Optional callback when a point is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun AreaChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    color: ChartyColor =
        ChartyColor.Gradient(
            listOf(
                ChartyColors.Blue,
                ChartyColors.BlueAlpha30,
            ),
        ),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    fillAlpha: Float = DEFAULT_FILL_ALPHA,
    onPointClick: ((LineData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Area chart data cannot be empty" }
    require(fillAlpha in 0f..1f) { "Fill alpha must be between 0 and 1" }

    val (minValue, maxValue) = rememberAreaValueRange(dataList, lineConfig.negativeValuesDrawMode)
    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = rememberChartAnimation(lineConfig.animation)
    val tooltipManager = rememberTooltipManager<Offset, LineData>()
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createAreaChartModifier(
        modifier = modifier,
        onPointClick = onPointClick,
        dataList = dataList,
        lineConfig = lineConfig,
        pointBounds = tooltipManager.bounds,
        onTooltipUpdate = tooltipManager::updateTooltip,
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createAxisConfig(minValue, maxValue, isBelowAxisMode),
        config = scaffoldConfig,
    ) { chartContext ->
        tooltipManager.clearBounds()
        val pointPositions = calculatePointPositions(dataList, chartContext) { tooltipManager.bounds.add(it) }
        val baselineY = calculateBaselineY(minValue, isBelowAxisMode, chartContext)

        drawAreaChart(
            params = AreaChartDrawParams(
                dataList = dataList,
                pointPositions = pointPositions,
                baselineY = baselineY,
                config = lineConfig,
                color = color,
                fillAlpha = fillAlpha,
                animationProgress = animationProgress.value,
                chartContext = chartContext,
                onBarBoundCalculated = { if (onPointClick != null) tooltipManager.bounds.add(it) },
            ),
        )

        drawTooltipHighlightIfNeeded(
            tooltipState = tooltipManager.tooltipState,
            lineConfig = lineConfig,
            pointBounds = tooltipManager.bounds,
            chartContext = chartContext,
            color = color,
        )
        drawTooltipIfNeeded(
            tooltipState = tooltipManager.tooltipState,
            lineConfig = lineConfig,
            textMeasurer = textMeasurer,
            chartContext = chartContext,
        )
    }
}

@Composable
private fun rememberAreaValueRange(
    dataList: List<LineData>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
): Pair<Float, Float> {
    return remember(dataList, negativeValuesDrawMode) {
        val values = dataList.getValues()
        val minValue = com.himanshoe.charty.common.util.calculateMinValue(values)
        val maxValue = com.himanshoe.charty.common.util.calculateMaxValue(values)
        minValue to maxValue
    }
}


private fun createAxisConfig(
    minValue: Float,
    maxValue: Float,
    isBelowAxisMode: Boolean,
): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = DEFAULT_AXIS_STEPS,
        drawAxisAtZero = isBelowAxisMode,
    )
}

private fun calculatePointPositions(
    dataList: List<LineData>,
    chartContext: com.himanshoe.charty.common.ChartContext,
    onPointCalculated: (Pair<Offset, LineData>) -> Unit,
): List<Offset> {
    return dataList.fastMapIndexed { index, point ->
        val position = Offset(
            x = chartContext.calculateCenteredXPosition(index, dataList.size),
            y = chartContext.convertValueToYPosition(point.value),
        )
        onPointCalculated(position to point)
        position
    }
}

private fun calculateBaselineY(
    minValue: Float,
    isBelowAxisMode: Boolean,
    chartContext: com.himanshoe.charty.common.ChartContext,
): Float {
    return if (minValue < 0f && isBelowAxisMode) {
        chartContext.convertValueToYPosition(0f)
    } else {
        chartContext.bottom
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAreaChart(params: AreaChartDrawParams) {
    if (params.pointPositions.isEmpty()) return

    // Draw filled area
    val areaPath = createAreaPath(params.pointPositions, params.baselineY, params.config.smoothCurve)
    val areaBrush = createAreaBrush(
        params.color,
        params.fillAlpha,
        params.chartContext.top,
        params.chartContext.bottom,
    )

    drawPath(
        path = areaPath,
        brush = areaBrush,
        style = Fill,
        alpha = params.animationProgress,
    )

    // Draw line
    val linePath = createLinePath(params.pointPositions, params.config.smoothCurve)
    val lineBrush = createLineBrush(params.color)

    drawPath(
        path = linePath,
        brush = lineBrush,
        style = Stroke(
            width = params.config.lineWidth,
            cap = params.config.strokeCap,
        ),
        alpha = params.animationProgress,
    )
    if (params.config.showPoints) {
        drawAreaPoints(params.pointPositions, lineBrush, params.config, params.animationProgress)
    }
}


private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAreaPoints(
    pointPositions: List<Offset>,
    lineBrush: Brush,
    config: LineChartConfig,
    animationProgress: Float,
) {
    pointPositions.fastForEachIndexed { index, position ->
        val pointProgress = index.toFloat() / (pointPositions.size - 1)
        if (pointProgress <= animationProgress) {
            drawCircle(
                brush = lineBrush,
                radius = config.pointRadius,
                center = position,
                alpha = config.pointAlpha,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTooltipHighlightIfNeeded(
    tooltipState: TooltipState?,
    lineConfig: LineChartConfig,
    pointBounds: List<Pair<Offset, LineData>>,
    chartContext: com.himanshoe.charty.common.ChartContext,
    color: ChartyColor,
) {
    tooltipState?.let { state ->
        val clickedPosition = pointBounds.find { (_, data) ->
            lineConfig.tooltipFormatter(data) == state.content
        }?.first

        clickedPosition?.let { position ->
            drawLine(
                color = Color.Black.copy(alpha = HIGHLIGHT_LINE_ALPHA),
                start = Offset(position.x, chartContext.top),
                end = Offset(position.x, chartContext.bottom),
                strokeWidth = HIGHLIGHT_LINE_WIDTH,
            )
            drawCircle(
                color = Color.White,
                radius = lineConfig.pointRadius + HIGHLIGHT_CIRCLE_OUTER_PADDING,
                center = position,
            )
            drawCircle(
                brush = Brush.linearGradient(color.value),
                radius = lineConfig.pointRadius + HIGHLIGHT_CIRCLE_INNER_PADDING,
                center = position,
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTooltipIfNeeded(
    tooltipState: TooltipState?,
    lineConfig: LineChartConfig,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    chartContext: com.himanshoe.charty.common.ChartContext,
) {
    tooltipState?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = lineConfig.tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}
