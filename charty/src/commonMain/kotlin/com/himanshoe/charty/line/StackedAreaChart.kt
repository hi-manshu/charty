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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
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
import com.himanshoe.charty.line.internal.stackedarea.StackedAreaChartConstants
import com.himanshoe.charty.line.internal.stackedarea.StackedAreaSeriesParams
import com.himanshoe.charty.line.internal.stackedarea.calculateCumulativePositions
import com.himanshoe.charty.line.internal.stackedarea.calculateLowerPositions
import com.himanshoe.charty.line.internal.stackedarea.drawStackedAreaSeries
import com.himanshoe.charty.line.internal.stackedarea.stackedAreaChartClickHandler

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
                Color(StackedAreaChartConstants.DEFAULT_COLOR_1),
                Color(StackedAreaChartConstants.DEFAULT_COLOR_2),
                Color(StackedAreaChartConstants.DEFAULT_COLOR_3),
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
    val areaSegmentBounds = remember {
        mutableListOf<Triple<Rect, androidx.compose.ui.graphics.Path, StackedAreaPoint>>()
    }
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
                Modifier.stackedAreaChartClickHandler(
                    dataList = dataList,
                    lineConfig = lineConfig,
                    areaSegmentBounds = areaSegmentBounds,
                    onAreaClick = onAreaClick,
                    onTooltipStateChange = { tooltipState = it },
                )
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

            val cumulativePositions = chartContext.calculateCumulativePositions(dataList, seriesIndex)
            val lowerPositions = chartContext.calculateLowerPositions(dataList, seriesIndex, baselineY)

            drawStackedAreaSeries(
                StackedAreaSeriesParams(
                    seriesIndex = seriesIndex,
                    seriesColor = seriesColor,
                    cumulativePositions = cumulativePositions,
                    lowerPositions = lowerPositions,
                    startX = startX,
                    baselineY = baselineY,
                    lineConfig = lineConfig,
                    fillAlpha = fillAlpha,
                    animationProgress = animationProgress.value,
                    dataList = dataList,
                    onSegmentBoundsCalculated = if (onAreaClick != null) {
                        { bounds -> areaSegmentBounds.add(bounds) }
                    } else {
                        null
                    },
                ),
            )
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
