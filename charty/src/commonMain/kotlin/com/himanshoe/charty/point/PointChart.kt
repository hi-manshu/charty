package com.himanshoe.charty.point

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.PointData

private const val TAP_RADIUS_MULTIPLIER = 2.5f
private const val POINT_RADIUS_MULTIPLIER = 2f
private const val HIGHLIGHT_CIRCLE_OUTER_OFFSET = 3f
private const val HIGHLIGHT_CIRCLE_INNER_OFFSET = 2f
private const val GUIDELINE_WIDTH = 1.5f
private const val GUIDELINE_ALPHA = 0.1f
private const val AXIS_STEPS = 6
private const val MIN_ANIMATION_PROGRESS = 0f
private const val MAX_ANIMATION_PROGRESS = 1f


private fun createChartModifier(
    modifier: Modifier,
    dataList: List<PointData>,
    pointConfig: PointChartConfig,
    pointBounds: List<Pair<Offset, PointData>>,
    onPointClick: ((PointData) -> Unit)?,
    onTooltipUpdate: (TooltipState?) -> Unit,
): Modifier = modifier.then(
    if (onPointClick != null) {
        Modifier.pointerInput(dataList, pointConfig, onPointClick) {
            detectTapGestures { offset ->
                val tapRadius = pointConfig.pointRadius * TAP_RADIUS_MULTIPLIER
                val clickedPoint = pointBounds.minByOrNull { (position, _) ->
                    calculateDistance(position, offset)
                }

                clickedPoint?.let { (position, pointData) ->
                    val distance = calculateDistance(position, offset)
                    if (distance <= tapRadius) {
                        onPointClick.invoke(pointData)
                        onTooltipUpdate(
                            TooltipState(
                                content = pointConfig.tooltipFormatter(pointData),
                                x = position.x - pointConfig.pointRadius,
                                y = position.y,
                                barWidth = pointConfig.pointRadius * POINT_RADIUS_MULTIPLIER,
                                position = pointConfig.tooltipPosition,
                            ),
                        )
                    } else {
                        onTooltipUpdate(null)
                    }
                } ?: run {
                    onTooltipUpdate(null)
                }
            }
        }
    } else {
        Modifier
    },
)

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
    val animationProgress = remember {
        Animatable(if (pointConfig.animation is Animation.Enabled) MIN_ANIMATION_PROGRESS else MAX_ANIMATION_PROGRESS)
    }
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val pointBounds = remember { mutableListOf<Pair<Offset, PointData>>() }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(pointConfig.animation) {
        if (pointConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = MAX_ANIMATION_PROGRESS,
                animationSpec = tween(durationMillis = pointConfig.animation.duration),
            )
        }
    }

    ChartScaffold(
        modifier = createChartModifier(
            modifier = modifier,
            dataList = dataList,
            pointConfig = pointConfig,
            pointBounds = pointBounds,
            onPointClick = onPointClick,
            onTooltipUpdate = { tooltipState = it },
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
        pointBounds.clear()

        dataList.fastForEachIndexed { index, point ->
            drawPointWithAnimation(
                point = point,
                index = index,
                dataListSize = dataList.size,
                animationProgress = animationProgress.value,
                chartContext = chartContext,
                pointConfig = pointConfig,
                color = color,
                pointBounds = pointBounds,
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

        tooltipState?.let { state ->
            drawTooltipHighlight(
                tooltipState = state,
                pointBounds = pointBounds,
                pointConfig = pointConfig,
                color = color,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
            )
        }
    }
}
