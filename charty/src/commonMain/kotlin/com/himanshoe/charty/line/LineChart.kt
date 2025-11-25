@file:Suppress(
    "LongMethod",
    "LongParameterList",
    "FunctionNaming",
    "CyclomaticComplexMethod",
    "WildcardImport",
    "MagicNumber",
    "MaxLineLength",
    "ReturnCount",
    "UnusedImports",
)

package com.himanshoe.charty.line

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.ext.calculateMaxValue
import com.himanshoe.charty.line.ext.calculateMinValue
import com.himanshoe.charty.line.ext.getLabels
import com.himanshoe.charty.line.ext.getValues

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
 *     color = ChartyColor.Solid(Color.Blue),
 *     lineConfig = LineChartConfig(
 *         lineWidth = 3f,
 *         showPoints = true,
 *         pointRadius = 6f,
 *         animation = Animation.Enabled()
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of line data points to display
 * @param modifier Modifier for the chart
 * @param color Color configuration for line and points
 * @param lineConfig Configuration for line appearance and behavior
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun LineChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(Color.Blue),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Line chart data cannot be empty" }

    val (minValue, maxValue) =
        remember(dataList, lineConfig.negativeValuesDrawMode) {
            val values = dataList.getValues()
            calculateMinValue(values) to calculateMaxValue(values)
        }

    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress =
        remember {
            Animatable(if (lineConfig.animation is Animation.Enabled) 0f else 1f)
        }

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
        modifier = modifier,
        xLabels = dataList.getLabels(),
        yAxisConfig =
            AxisConfig(
                minValue = minValue,
                maxValue = maxValue,
                steps = 6,
                // When using FROM_MIN_VALUE mode, always draw axis at bottom (not centered at zero)
                drawAxisAtZero = isBelowAxisMode,
            ),
        config = scaffoldConfig,
    ) { chartContext ->
        val pointPositions =
            dataList.fastMapIndexed { index, point ->
                Offset(
                    x = chartContext.calculateCenteredXPosition(index, dataList.size),
                    y = chartContext.convertValueToYPosition(point.value),
                )
            }

        if (lineConfig.smoothCurve) {
            val path = Path()

            if (pointPositions.isNotEmpty()) {
                path.moveTo(pointPositions[0].x, pointPositions[0].y)

                for (i in 0 until pointPositions.size - 1) {
                    val current = pointPositions[i]
                    val next = pointPositions[i + 1]

                    val controlPoint1X = current.x + (next.x - current.x) / 3f
                    val controlPoint1Y = current.y
                    val controlPoint2X = current.x + 2 * (next.x - current.x) / 3f
                    val controlPoint2Y = next.y

                    path.cubicTo(
                        controlPoint1X,
                        controlPoint1Y,
                        controlPoint2X,
                        controlPoint2Y,
                        next.x,
                        next.y,
                    )
                }

                drawPath(
                    path = path,
                    brush = Brush.linearGradient(color.value),
                    style =
                        Stroke(
                            width = lineConfig.lineWidth,
                            cap = lineConfig.strokeCap,
                        ),
                    alpha = animationProgress.value,
                )
            }
        } else {
            // Draw straight lines connecting consecutive points with animation
            val segmentsToDraw = ((pointPositions.size - 1) * animationProgress.value).toInt()
            val segmentProgress = ((pointPositions.size - 1) * animationProgress.value) - segmentsToDraw

            for (i in 0 until segmentsToDraw) {
                drawLine(
                    brush = Brush.linearGradient(color.value),
                    start = pointPositions[i],
                    end = pointPositions[i + 1],
                    strokeWidth = lineConfig.lineWidth,
                    cap = lineConfig.strokeCap,
                )
            }

            // Draw partial segment for smooth animation
            if (segmentsToDraw < pointPositions.size - 1 && segmentProgress > 0) {
                val start = pointPositions[segmentsToDraw]
                val end = pointPositions[segmentsToDraw + 1]
                val partialEnd =
                    Offset(
                        x = start.x + (end.x - start.x) * segmentProgress,
                        y = start.y + (end.y - start.y) * segmentProgress,
                    )
                drawLine(
                    brush = Brush.linearGradient(color.value),
                    start = start,
                    end = partialEnd,
                    strokeWidth = lineConfig.lineWidth,
                    cap = lineConfig.strokeCap,
                )
            }
        }

        // Optionally draw circular markers at data points with fastForEach
        if (lineConfig.showPoints) {
            pointPositions.fastForEachIndexed { index, position ->
                // Only draw points up to animation progress
                val pointProgress = index.toFloat() / (pointPositions.size - 1)
                if (pointProgress <= animationProgress.value) {
                    drawCircle(
                        brush = Brush.linearGradient(color.value),
                        radius = lineConfig.pointRadius,
                        center = position,
                        alpha = lineConfig.pointAlpha,
                    )
                }
            }
        }

        // Draw reference / target line if configured
        lineConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer,
            )
        }
    }
}
