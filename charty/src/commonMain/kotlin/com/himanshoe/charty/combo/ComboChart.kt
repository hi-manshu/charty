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

package com.himanshoe.charty.combo

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.combo.ext.getAllValues
import com.himanshoe.charty.combo.ext.getLabels
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import kotlin.math.max
import kotlin.math.min

/**
 * Combo Chart - Display data as both bars and line in the same chart
 *
 * A combo chart combines bar chart and line chart visualizations, allowing you to
 * compare two related data series with different visual representations. Ideal for
 * showing trends alongside categorical values.
 *
 * Usage:
 * ```kotlin
 * ComboChart(
 *     data = {
 *         listOf(
 *             ComboChartData("Jan", barValue = 100f, lineValue = 80f),
 *             ComboChartData("Feb", barValue = 150f, lineValue = 120f),
 *             ComboChartData("Mar", barValue = 120f, lineValue = 140f),
 *             ComboChartData("Apr", barValue = 180f, lineValue = 160f)
 *         )
 *     },
 *     barColor = ChartyColor.Solid(Color.Blue),
 *     lineColor = ChartyColor.Solid(Color.Red),
 *     comboConfig = ComboChartConfig(
 *         barWidthFraction = 0.6f,
 *         lineWidth = 3f,
 *         showPoints = true,
 *         animation = Animation.Enabled()
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of combo chart data points to display
 * @param modifier Modifier for the chart
 * @param barColor Color configuration for bars
 * @param lineColor Color configuration for line and points
 * @param comboConfig Configuration for both bar and line appearance and behavior
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onDataClick Optional callback when a data point is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun ComboChart(
    data: () -> List<ComboChartData>,
    modifier: Modifier = Modifier,
    barColor: ChartyColor = ChartyColor.Solid(Color.Blue),
    lineColor: ChartyColor = ChartyColor.Solid(Color.Red),
    comboConfig: ComboChartConfig = ComboChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onDataClick: ((ComboChartData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Combo chart data cannot be empty" }

    val (minValue, maxValue) =
        remember(dataList, comboConfig.negativeValuesDrawMode) {
            val allValues = dataList.getAllValues()
            val calculatedMin = allValues.minOrNull() ?: 0f
            val calculatedMax = allValues.maxOrNull() ?: 0f

            val minVal =
                if (comboConfig.negativeValuesDrawMode ==
                    com.himanshoe.charty.bar.config.NegativeValuesDrawMode.BELOW_AXIS
                ) {
                    min(calculatedMin, 0f)
                } else {
                    calculatedMin
                }

            val maxVal = max(calculatedMax, if (minVal < 0f) 0f else calculatedMin)
            minVal to maxVal
        }

    val isBelowAxisMode =
        comboConfig.negativeValuesDrawMode ==
            com.himanshoe.charty.bar.config.NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress =
        remember {
            Animatable(if (comboConfig.animation is Animation.Enabled) 0f else 1f)
        }

    // State to track which data point is currently showing a tooltip
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }

    // Store data point bounds for hit testing (both bars and line points)
    val dataBounds = remember { mutableListOf<Pair<Rect, ComboChartData>>() }

    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(comboConfig.animation) {
        if (comboConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = comboConfig.animation.duration),
            )
        }
    }


    ChartScaffold(
        modifier = modifier.then(
            if (onDataClick != null) {
                Modifier.pointerInput(dataList, comboConfig, onDataClick) {
                    detectTapGestures { offset ->
                        val clickedData = dataBounds.find { (rect, _) ->
                            rect.contains(offset)
                        }

                        clickedData?.let { (rect, comboData) ->
                            onDataClick.invoke(comboData)
                            tooltipState = TooltipState(
                                content = comboConfig.tooltipFormatter(comboData),
                                x = rect.left,
                                y = rect.top,
                                barWidth = rect.width,
                                position = comboConfig.tooltipPosition,
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
        yAxisConfig =
            AxisConfig(
                minValue = minValue,
                maxValue = maxValue,
                steps = 6,
                drawAxisAtZero = isBelowAxisMode,
            ),
        config = scaffoldConfig,
    ) { chartContext ->
        dataBounds.clear()

        val baselineY =
            if (minValue < 0f && isBelowAxisMode) {
                chartContext.convertValueToYPosition(0f)
            } else {
                chartContext.bottom
            }

        // Draw bars first (background layer)
        dataList.fastForEachIndexed { index, comboData ->
            val barX =
                chartContext.calculateBarLeftPosition(
                    index,
                    dataList.size,
                    comboConfig.barWidthFraction,
                )
            val barWidth =
                chartContext.calculateBarWidth(
                    dataList.size,
                    comboConfig.barWidthFraction,
                )
            val barValueY = chartContext.convertValueToYPosition(comboData.barValue)
            val isNegative = comboData.barValue < 0f

            val barTop: Float
            val barHeight: Float

            if (isNegative) {
                barTop = baselineY
                val fullBarHeight = barValueY - baselineY
                barHeight = fullBarHeight * animationProgress.value
            } else {
                val fullBarHeight = baselineY - barValueY
                val animatedBarHeight = fullBarHeight * animationProgress.value
                barTop = baselineY - animatedBarHeight
                barHeight = animatedBarHeight
            }

            // Store bar bounds for hit testing
            if (onDataClick != null && barHeight > 0) {
                dataBounds.add(
                    Rect(
                        left = barX,
                        top = barTop,
                        right = barX + barWidth,
                        bottom = barTop + barHeight,
                    ) to comboData,
                )
            }

            val brush = with(chartContext) { barColor.toVerticalGradientBrush() }

            drawRoundedBar(
                brush = brush,
                x = barX,
                y = barTop,
                width = barWidth,
                height = barHeight,
                isNegative = isNegative,
                isBelowAxisMode = isBelowAxisMode,
                cornerRadius = comboConfig.barCornerRadius.value,
            )
        }

        // Draw line on top (foreground layer)
        val pointPositions =
            dataList.fastMapIndexed { index, comboData ->
                Offset(
                    x = chartContext.calculateCenteredXPosition(index, dataList.size),
                    y = chartContext.convertValueToYPosition(comboData.lineValue),
                )
            }

        if (comboConfig.smoothCurve) {
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
                    brush = Brush.linearGradient(lineColor.value),
                    style =
                        Stroke(
                            width = comboConfig.lineWidth,
                            cap = comboConfig.strokeCap,
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
                    brush = Brush.linearGradient(lineColor.value),
                    start = pointPositions[i],
                    end = pointPositions[i + 1],
                    strokeWidth = comboConfig.lineWidth,
                    cap = comboConfig.strokeCap,
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
                    brush = Brush.linearGradient(lineColor.value),
                    start = start,
                    end = partialEnd,
                    strokeWidth = comboConfig.lineWidth,
                    cap = comboConfig.strokeCap,
                )
            }
        }

        // Draw circular markers at data points
        if (comboConfig.showPoints) {
            pointPositions.fastForEachIndexed { index, position ->
                // Only draw points up to animation progress
                val pointProgress = index.toFloat() / (pointPositions.size - 1)
                if (pointProgress <= animationProgress.value) {
                    // Store point bounds for hit testing (larger hit area than visual)
                    if (onDataClick != null) {
                        val hitRadius = comboConfig.pointRadius * 2f
                        dataBounds.add(
                            Rect(
                                left = position.x - hitRadius,
                                top = position.y - hitRadius,
                                right = position.x + hitRadius,
                                bottom = position.y + hitRadius,
                            ) to dataList[index],
                        )
                    }

                    drawCircle(
                        brush = Brush.linearGradient(lineColor.value),
                        radius = comboConfig.pointRadius,
                        center = position,
                        alpha = comboConfig.pointAlpha,
                    )
                }
            }
        }

        // Draw reference / target line if configured
        comboConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer,
            )
        }

        // Draw tooltip
        tooltipState?.let { state ->
            drawTooltip(
                tooltipState = state,
                config = comboConfig.tooltipConfig,
                textMeasurer = textMeasurer,
                chartWidth = chartContext.right,
                chartTop = chartContext.top,
                chartBottom = chartContext.bottom,
            )
        }
    }
}

/**
 * Helper function to draw a bar with rounded corners based on bar position
 */
private fun DrawScope.drawRoundedBar(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    cornerRadius: Float,
) {
    val path =
        Path().apply {
            if (isNegative && isBelowAxisMode) {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    ),
                )
            } else {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomLeftCornerRadius = CornerRadius.Zero,
                        bottomRightCornerRadius = CornerRadius.Zero,
                    ),
                )
            }
        }
    drawPath(path, brush)
}
