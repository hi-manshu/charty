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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.StackedAreaPoint
import com.himanshoe.charty.line.ext.calculateMaxValue
import com.himanshoe.charty.line.ext.getLabels

/**
 * Stacked Area Chart - Display multiple series as stacked filled areas
 *
 * A stacked area chart displays multiple data series as filled areas stacked on top of each other.
 * Each area shows the cumulative total, making it ideal for visualizing part-to-whole relationships
 * and showing how the composition changes over time. Starts smoothly from the axis intersection (0,0).
 *
 * Usage:
 * ```kotlin
 * StackedAreaChart(
 *     data = {
 *         listOf(
 *             LineGroup("Mon", listOf(20f, 15f, 10f)),
 *             LineGroup("Tue", listOf(45f, 28f, 12f)),
 *             LineGroup("Wed", listOf(30f, 22f, 18f)),
 *             LineGroup("Thu", listOf(70f, 30f, 15f))
 *         )
 *     },
 *     colors = ChartyColor.Gradient(
 *         listOf(
 *             Color(0xFF2196F3),
 *             Color(0xFF4CAF50),
 *             Color(0xFFFF9800)
 *         )
 *     ),
 *     lineConfig = LineChartConfig(
 *         lineWidth = 2f,
 *         smoothCurve = true
 *     ),
 *     fillAlpha = 0.7f
 * )
 * ```
 *
 * @param data Lambda returning list of line groups, each containing values for all series at that X position
 * @param data Lambda returning list of line groups, each containing values for all series at that X position
 * @param modifier Modifier for the chart
 * @param colors Color configuration - Gradient recommended for distinguishing stacked areas
 * @param lineConfig Configuration for line appearance and behavior
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param fillAlpha Alpha transparency for the filled areas (0.0f - 1.0f)
 * @param onAreaClick Optional callback when an area is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun StackedAreaChart(
    data: () -> List<LineGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor =
        ChartyColor.Gradient(
            listOf(
                Color(0xFF2196F3),
                Color(0xFF4CAF50),
                Color(0xFFFF9800),
            ),
        ),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    fillAlpha: Float = 0.7f,
    onAreaClick: ((StackedAreaPoint) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Stacked area chart data cannot be empty" }
    require(fillAlpha in 0f..1f) { "Fill alpha must be between 0 and 1" }

    val (maxValue, colorList) =
        remember(dataList, colors) {
            val allStackedValues =
                dataList.flatMap { group ->
                    val cumulativeValues = mutableListOf<Float>()
                    var sum = 0f
                    group.values.forEach { value ->
                        sum += value
                        cumulativeValues.add(sum)
                    }
                    cumulativeValues
                }
            calculateMaxValue(allStackedValues) to colors.value
        }

    val minValue = 0f
    val animationProgress = remember {
            Animatable(if (lineConfig.animation is Animation.Enabled) 0f else 1f)
        }
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val areaSegmentBounds = remember { mutableListOf<Triple<Rect, Path, StackedAreaPoint>>() }
    val textMeasurer = rememberTextMeasurer()
    LaunchedEffect(lineConfig.animation) {
        if (lineConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = lineConfig.animation.duration),
            )
        }
    }

    ChartScaffold(
        modifier = modifier.then(
            if (onAreaClick != null) {
                Modifier.pointerInput(dataList, lineConfig, onAreaClick) {
                    detectTapGestures { offset ->
                        val clickedSegment = areaSegmentBounds.find { (bounds, _, _) ->
                            bounds.contains(offset)
                        }

                        clickedSegment?.let { (bounds, _, areaPoint) ->
                            onAreaClick.invoke(areaPoint)
                            tooltipState = TooltipState(
                                content = "${areaPoint.lineGroup.label}: ${areaPoint.value}",
                                x = bounds.left + bounds.width / 2,
                                y = bounds.top,
                                barWidth = bounds.width,
                                position = lineConfig.tooltipPosition,
                            )
                        } ?: run {
                            tooltipState = null
                        }
                    }
                }
            } else {
                Modifier
            },
        ),
        xLabels = dataList.getLabels(),
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = 6,
            drawAxisAtZero = false,
        ),
        config = scaffoldConfig,
    ) { chartContext ->
        areaSegmentBounds.clear()

        val baselineY = chartContext.bottom
        val startX = chartContext.left
        val seriesCount = dataList.firstOrNull()?.values?.size ?: 0
        for (seriesIndex in seriesCount - 1 downTo 0) {
            val seriesColor = colorList[seriesIndex % colorList.size]

            // Calculate cumulative positions for this series
            val cumulativePositions = dataList.fastMapIndexed { index, group ->
                var cumulativeValue = 0f
                for (i in 0..seriesIndex) {
                    cumulativeValue += group.values.getOrNull(i) ?: 0f
                }
                Offset(
                    x = chartContext.calculateCenteredXPosition(index, dataList.size),
                    y = chartContext.convertValueToYPosition(cumulativeValue),
                )
            }

            // Calculate lower bound positions (previous series cumulative or baseline)
            val lowerPositions = if (seriesIndex > 0) {
                dataList.fastMapIndexed { index, group ->
                    var cumulativeValue = 0f
                    for (i in 0 until seriesIndex) {
                        cumulativeValue += group.values.getOrNull(i) ?: 0f
                    }
                    Offset(
                        x = chartContext.calculateCenteredXPosition(index, dataList.size),
                        y = chartContext.convertValueToYPosition(cumulativeValue),
                    )
                }
            } else {
                dataList.fastMapIndexed { index, _ ->
                    Offset(
                        x = chartContext.calculateCenteredXPosition(index, dataList.size),
                        y = baselineY,
                    )
                }
            }

            if (cumulativePositions.isNotEmpty()) {
                val areaPath =
                    Path().apply {
                        val firstPoint = cumulativePositions[0]
                        if (lineConfig.smoothCurve) {
                            val control1X = startX + (firstPoint.x - startX) / 3f
                            val control2X = startX + 2 * (firstPoint.x - startX) / 3f
                            val control2Y = firstPoint.y
                            moveTo(startX, baselineY)
                            cubicTo(control1X, baselineY, control2X, control2Y, firstPoint.x, firstPoint.y)
                            for (i in 0 until cumulativePositions.size - 1) {
                                val current = cumulativePositions[i]
                                val next = cumulativePositions[i + 1]
                                val controlPoint1X = current.x + (next.x - current.x) / 3f
                                val controlPoint1Y = current.y
                                val controlPoint2X = current.x + 2 * (next.x - current.x) / 3f
                                val controlPoint2Y = next.y
                                cubicTo(
                                    controlPoint1X,
                                    controlPoint1Y,
                                    controlPoint2X,
                                    controlPoint2Y,
                                    next.x,
                                    next.y,
                                )
                            }
                            lineTo(cumulativePositions.last().x, baselineY)
                            lineTo(startX, baselineY)
                        } else {
                            moveTo(startX, baselineY)
                            lineTo(firstPoint.x, firstPoint.y)
                            for (i in 1 until cumulativePositions.size) {
                                lineTo(cumulativePositions[i].x, cumulativePositions[i].y)
                            }
                            lineTo(cumulativePositions.last().x, baselineY)
                            lineTo(startX, baselineY)
                        }

                        close()
                    }
                drawPath(
                    path = areaPath,
                    color = seriesColor.copy(alpha = fillAlpha),
                    style = Fill,
                    alpha = animationProgress.value,
                )

                // Draw top border line - ALL series start from axis intersection
                val linePath =
                    Path().apply {
                        val firstPoint = cumulativePositions[0]

                        if (lineConfig.smoothCurve) {
                            // Smooth cubic start from (0,0) for ALL series
                            val control1X = startX + (firstPoint.x - startX) / 3f
                            val control2X = startX + 2 * (firstPoint.x - startX) / 3f
                            val control2Y = firstPoint.y
                            moveTo(startX, baselineY)
                            cubicTo(control1X, baselineY, control2X, control2Y, firstPoint.x, firstPoint.y)

                            // Draw smooth curve through data points
                            for (i in 0 until cumulativePositions.size - 1) {
                                val current = cumulativePositions[i]
                                val next = cumulativePositions[i + 1]

                                val controlPoint1X = current.x + (next.x - current.x) / 3f
                                val controlPoint1Y = current.y
                                val controlPoint2X = current.x + 2 * (next.x - current.x) / 3f
                                val controlPoint2Y = next.y

                                cubicTo(
                                    controlPoint1X,
                                    controlPoint1Y,
                                    controlPoint2X,
                                    controlPoint2Y,
                                    next.x,
                                    next.y,
                                )
                            }
                        } else {
                            moveTo(startX, baselineY)
                            lineTo(firstPoint.x, firstPoint.y)

                            for (i in 1 until cumulativePositions.size) {
                                lineTo(cumulativePositions[i].x, cumulativePositions[i].y)
                            }
                        }
                    }

                drawPath(
                    path = linePath,
                    color = seriesColor,
                    style =
                        Stroke(
                            width = lineConfig.lineWidth,
                            cap = lineConfig.strokeCap,
                        ),
                    alpha = animationProgress.value,
                )
                if (onAreaClick != null) {
                    dataList.fastForEachIndexed { dataIndex, group ->
                        val segmentValue = group.values.getOrNull(seriesIndex) ?: 0f
                        val upperPoint = cumulativePositions.getOrNull(dataIndex)
                        val lowerPoint = lowerPositions.getOrNull(dataIndex)

                        if (upperPoint != null && lowerPoint != null && dataIndex < cumulativePositions.size - 1) {
                            val nextUpperPoint = cumulativePositions[dataIndex + 1]
                            val nextLowerPoint = lowerPositions[dataIndex + 1]

                            val segmentPath = Path().apply {
                                moveTo(upperPoint.x, upperPoint.y)
                                lineTo(nextUpperPoint.x, nextUpperPoint.y)
                                lineTo(nextLowerPoint.x, nextLowerPoint.y)
                                lineTo(lowerPoint.x, lowerPoint.y)
                                close()
                            }

                            // Calculate bounds for this segment
                            val minX = minOf(upperPoint.x, lowerPoint.x, nextUpperPoint.x, nextLowerPoint.x)
                            val maxX = maxOf(upperPoint.x, lowerPoint.x, nextUpperPoint.x, nextLowerPoint.x)
                            val minY = minOf(upperPoint.y, lowerPoint.y, nextUpperPoint.y, nextLowerPoint.y)
                            val maxY = maxOf(upperPoint.y, lowerPoint.y, nextUpperPoint.y, nextLowerPoint.y)

                            var cumulativeValue = 0f
                            for (i in 0..seriesIndex) {
                                cumulativeValue += group.values.getOrNull(i) ?: 0f
                            }

                            areaSegmentBounds.add(
                                Triple(
                                    Rect(left = minX, top = minY, right = maxX, bottom = maxY),
                                    segmentPath,
                                    StackedAreaPoint(
                                        lineGroup = group,
                                        seriesIndex = seriesIndex,
                                        dataIndex = dataIndex,
                                        value = segmentValue,
                                        cumulativeValue = cumulativeValue,
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }

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
}
