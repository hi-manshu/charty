package com.himanshoe.charty.point

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.constants.ChartConstants
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.gesture.createPointTooltipState
import com.himanshoe.charty.common.gesture.pointChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.PointData

// Use constants from ChartConstants for consistency across charts
private const val TAP_RADIUS_MULTIPLIER = ChartConstants.DEFAULT_TAP_RADIUS_MULTIPLIER
private const val POINT_RADIUS_MULTIPLIER = ChartConstants.DEFAULT_HIGHLIGHT_RADIUS_MULTIPLIER
private const val HIGHLIGHT_CIRCLE_OUTER_OFFSET = ChartConstants.DEFAULT_HIGHLIGHT_OUTER_OFFSET
private const val HIGHLIGHT_CIRCLE_INNER_OFFSET = ChartConstants.DEFAULT_HIGHLIGHT_INNER_OFFSET
private const val GUIDELINE_WIDTH = ChartConstants.DEFAULT_GUIDELINE_WIDTH
private const val GUIDELINE_ALPHA = ChartConstants.DEFAULT_GUIDELINE_ALPHA
private const val AXIS_STEPS = ChartConstants.DEFAULT_AXIS_STEPS
private const val MIN_ANIMATION_PROGRESS = ChartConstants.MIN_ANIMATION_PROGRESS
private const val MAX_ANIMATION_PROGRESS = ChartConstants.MAX_ANIMATION_PROGRESS


/**
 * Creates a modifier with click handling for point chart.
 */
private fun createChartModifier(
    modifier: Modifier,
    dataList: List<PointData>,
    pointConfig: PointChartConfig,
    pointBounds: List<Pair<Offset, PointData>>,
    onPointClick: ((PointData) -> Unit)?,
    onTooltipUpdate: (TooltipState?) -> Unit,
): Modifier {
    return if (onPointClick != null) {
        modifier.pointChartClickHandler(
            dataList = dataList,
            pointBounds = pointBounds,
            tapRadius = pointConfig.pointRadius * TAP_RADIUS_MULTIPLIER,
            onPointClick = onPointClick,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = { pointData, position ->
                createPointTooltipState(
                    content = pointConfig.tooltipFormatter(pointData),
                    position = position,
                    pointRadius = pointConfig.pointRadius,
                    tooltipPosition = pointConfig.tooltipPosition,
                    pointRadiusMultiplier = POINT_RADIUS_MULTIPLIER,
                )
            }
        )
    } else {
        modifier
    }
}

private fun DrawScope.drawPointWithAnimation(
    point: PointData,
    index: Int,
    dataListSize: Int,
    animationProgress: Float,
    chartContext: com.himanshoe.charty.common.ChartContext,
    pointConfig: PointChartConfig,
    color: ChartyColor,
    pointBounds: MutableList<Pair<Offset, PointData>>,
    addToBounds: Boolean,
) {
    val pointProgress = index.toFloat() / dataListSize
    val progressOffset = (animationProgress - pointProgress) * dataListSize
    val pointAnimationProgress = progressOffset.coerceIn(
        MIN_ANIMATION_PROGRESS,
        MAX_ANIMATION_PROGRESS,
    )

    val pointX = chartContext.calculateCenteredXPosition(index, dataListSize)
    val pointY = chartContext.convertValueToYPosition(point.value)
    val position = Offset(pointX, pointY)

    if (addToBounds) {
        pointBounds.add(position to point)
    }

    if (pointAnimationProgress > MIN_ANIMATION_PROGRESS) {
        drawCircle(
            brush = Brush.linearGradient(color.value),
            radius = pointConfig.pointRadius * pointAnimationProgress,
            center = position,
            alpha = pointConfig.pointAlpha * pointAnimationProgress,
        )
    }
}

private fun DrawScope.drawTooltipHighlight(
    tooltipState: TooltipState,
    pointBounds: List<Pair<Offset, PointData>>,
    pointConfig: PointChartConfig,
    color: ChartyColor,
    chartContext: com.himanshoe.charty.common.ChartContext,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    val clickedPosition = pointBounds.find { (_, data) ->
        pointConfig.tooltipFormatter(data) == tooltipState.content
    }?.first

    clickedPosition?.let { position ->
        drawLine(
            color = Color.Black.copy(alpha = GUIDELINE_ALPHA),
            start = Offset(position.x, chartContext.top),
            end = Offset(position.x, chartContext.bottom),
            strokeWidth = GUIDELINE_WIDTH,
        )
        drawCircle(
            color = Color.White,
            radius = pointConfig.pointRadius + HIGHLIGHT_CIRCLE_OUTER_OFFSET,
            center = position,
        )
        drawCircle(
            brush = Brush.linearGradient(color.value),
            radius = pointConfig.pointRadius + HIGHLIGHT_CIRCLE_INNER_OFFSET,
            center = position,
        )
    }

    drawTooltip(
        tooltipState = tooltipState,
        config = pointConfig.tooltipConfig,
        textMeasurer = textMeasurer,
        chartWidth = chartContext.right,
        chartTop = chartContext.top,
        chartBottom = chartContext.bottom,
    )
}

/**
 * Point Chart - Display data as individual points with optional click interactions.
 *
 * @param data Lambda returning list of point data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration for points
 * @param pointConfig Configuration for point appearance and behavior
 * @param scaffoldConfig Chart styling configuration
 * @param onPointClick Optional callback when a point is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun PointChart(
    data: () -> List<PointData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    pointConfig: PointChartConfig = PointChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onPointClick: ((PointData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Point chart data cannot be empty" }

    val (minValue, maxValue) = remember(dataList, pointConfig.negativeValuesDrawMode) {
        val values = dataList.getValues()
        calculateMinValue(values) to calculateMaxValue(values)
    }

    val isBelowAxisMode = pointConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress = rememberChartAnimation(
        animation = pointConfig.animation,
        initialValue = null,
        targetValue = MAX_ANIMATION_PROGRESS
    )

    val tooltipManager = rememberTooltipManager<Offset, PointData>()
    val textMeasurer = rememberTextMeasurer()


    ChartScaffold(
        modifier = createChartModifier(
            modifier = modifier,
            dataList = dataList,
            pointConfig = pointConfig,
            pointBounds = tooltipManager.bounds,
            onPointClick = onPointClick,
            onTooltipUpdate = tooltipManager::updateTooltip,
        ),
        xLabels = dataList.getLabels(),
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = AXIS_STEPS,
            drawAxisAtZero = isBelowAxisMode,
        ),
        config = scaffoldConfig,
    ) { chartContext ->
        tooltipManager.clearBounds()

        dataList.fastForEachIndexed { index, point ->
            drawPointWithAnimation(
                point = point,
                index = index,
                dataListSize = dataList.size,
                animationProgress = animationProgress.value,
                chartContext = chartContext,
                pointConfig = pointConfig,
                color = color,
                pointBounds = tooltipManager.bounds,
                addToBounds = onPointClick != null,
            )
        }

        pointConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer,
            )
        }

        tooltipManager.tooltipState?.let { state ->
            drawTooltipHighlight(
                tooltipState = state,
                pointBounds = tooltipManager.bounds,
                pointConfig = pointConfig,
                color = color,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
            )
        }
    }
}
