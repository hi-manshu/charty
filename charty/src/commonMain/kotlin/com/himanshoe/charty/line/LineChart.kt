package com.himanshoe.charty.line

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
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
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.ext.calculateMaxValue
import com.himanshoe.charty.line.ext.calculateMinValue
import com.himanshoe.charty.line.ext.getLabels
import com.himanshoe.charty.line.ext.getValues
import com.himanshoe.charty.line.internal.line.calculatePointPositions
import com.himanshoe.charty.line.internal.line.drawAnimatedPoints
import com.himanshoe.charty.line.internal.line.drawLineChartTooltip
import com.himanshoe.charty.line.internal.line.drawSmoothLine
import com.himanshoe.charty.line.internal.line.drawStraightLineSegments
import com.himanshoe.charty.line.internal.line.lineChartClickHandler

/**
 * Line Chart - Connect data points with lines
 *
 * A line chart displays information as a series of data points connected by straight line segments.
 * It is useful for showing trends over time or continuous data.
 *
 * Usage:
 * ```kotlin
 * LineChart(
 *     data = {
 *         listOf(
 *             LineData("Mon", 20f),
 *             LineData("Tue", 45f),
 *             LineData("Wed", 30f),
 *             LineData("Thu", 70f)
 *         )
 *     },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 *     lineConfig = LineChartConfig(
 *         lineWidth = 3f,
 *         showPoints = true,
 *         pointRadius = 6f,
 *         animation = Animation.Enabled()
 *     ),
 *     onPointClick = { lineData ->
 *         println("Clicked: ${lineData.label}")
 *     }
 * )
 * ```
 *
 * @param data Lambda returning list of line data points to display
 * @param modifier Modifier for the chart
 * @param color Color configuration for line and points
 * @param lineConfig Configuration for line appearance and behavior
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onPointClick Optional callback when a point is clicked
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
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Line chart data cannot be empty" }
    val (minValue, maxValue) = remember(dataList, lineConfig.negativeValuesDrawMode) {
        val values = dataList.getValues()
        calculateMinValue(values) to calculateMaxValue(values)
    }
    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = remember {
        Animatable(if (lineConfig.animation is Animation.Enabled) 0f else 1f)
    }
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val pointBounds = remember { mutableListOf<Pair<Offset, LineData>>() }
    LaunchedEffect(lineConfig.animation) {
        if (lineConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = lineConfig.animation.duration),
            )
        }
    }
    val textMeasurer = rememberTextMeasurer()
    ChartScaffold(
        modifier = modifier.then(
            if (onPointClick != null) {
                Modifier.lineChartClickHandler(
                    dataList = dataList,
                    lineConfig = lineConfig,
                    pointBounds = pointBounds,
                    onPointClick = onPointClick,
                    onTooltipStateChange = { tooltipState = it },
                )
            } else {
                Modifier
            },
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
    ) { chartContext ->
        pointBounds.clear()

        val pointPositions = chartContext.calculatePointPositions(dataList)
        if (onPointClick != null) {
            pointPositions.forEachIndexed { index, position ->
                pointBounds.add(position to dataList[index])
            }
        }

        if (lineConfig.smoothCurve) {
            drawSmoothLine(
                pointPositions = pointPositions,
                color = color,
                lineConfig = lineConfig,
                animationProgress = animationProgress.value,
            )
        } else {
            drawStraightLineSegments(
                pointPositions = pointPositions,
                color = color,
                lineConfig = lineConfig,
                animationProgress = animationProgress.value,
            )
        }

        if (lineConfig.showPoints) {
            drawAnimatedPoints(
                pointPositions = pointPositions,
                color = color,
                lineConfig = lineConfig,
                animationProgress = animationProgress.value,
            )
        }

        lineConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer,
            )
        }

        tooltipState?.let { state ->
            drawLineChartTooltip(
                tooltipState = state,
                pointBounds = pointBounds,
                color = color,
                lineConfig = lineConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
            )
        }
    }
}
