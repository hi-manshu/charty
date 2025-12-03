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
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.internal.line.calculatePointPositions
import com.himanshoe.charty.line.internal.line.drawAnimatedPoints
import com.himanshoe.charty.line.internal.line.drawLineChartTooltip
import com.himanshoe.charty.line.internal.line.drawSmoothLine
import com.himanshoe.charty.line.internal.line.drawStraightLineSegments
import com.himanshoe.charty.line.internal.line.lineChartClickHandler

/**
 * A composable function that displays a line chart.
 *
 * A line chart represents information as a series of data points connected by straight line segments.
 * It is particularly useful for visualizing trends over time or continuous data.
 *
 * @param data A lambda function that returns a list of [LineData] points to be displayed.
 * @param modifier The modifier to be applied to the chart.
 * @param color The color or color scheme for the line and its points, defined by a [ChartyColor].
 * @param lineConfig The configuration for the line's appearance and behavior, such as line width and point visibility, defined by a [LineChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels, defined by a [ChartScaffoldConfig].
 * @param onPointClick A lambda function to be invoked when a point on the line is clicked, providing the corresponding [LineData].
 *
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
    val animationProgress = rememberChartAnimation(lineConfig.animation)

    val tooltipManager = rememberTooltipManager<Offset, LineData>()
    val textMeasurer = rememberTextMeasurer()
    ChartScaffold(
        modifier = modifier.then(
            if (onPointClick != null) {
                Modifier.lineChartClickHandler(
                    dataList = dataList,
                    lineConfig = lineConfig,
                    pointBounds = tooltipManager.bounds,
                    onPointClick = onPointClick,
                    onTooltipStateChange = tooltipManager::updateTooltip,
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
        tooltipManager.clearBounds()

        val pointPositions = chartContext.calculatePointPositions(dataList)
        if (onPointClick != null) {
            pointPositions.fastForEachIndexed { index, position ->
                tooltipManager.bounds.add(position to dataList[index])
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
