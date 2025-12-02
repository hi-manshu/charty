package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.horizontal.HorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.horizontal.calculateHorizontalAxisOffset
import com.himanshoe.charty.bar.internal.bar.horizontal.calculateHorizontalBaselineX
import com.himanshoe.charty.bar.internal.bar.horizontal.createHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalBars
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.horizontal.rememberHorizontalAnimation
import com.himanshoe.charty.bar.internal.bar.horizontal.rememberHorizontalValueRange
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Horizontal Bar Chart - Display data as horizontal bars
 *
 * A horizontal bar chart presents categorical data with horizontal rectangular bars.
 * The length of each bar is proportional to the value it represents.
 * Ideal for comparing categories with long labels or when you have many categories.
 *
 * Usage:
 * ```kotlin
 * HorizontalBarChart(
 *     data = {
 *         listOf(
 *             BarData("Category A", 100f),
 *             BarData("Category B", 150f),
 *             BarData("Category C", 120f)
 *         )
 *     },
 *     color = ChartyColor.Solid(Color(0xFF2196F3)),
 *     barConfig = BarChartConfig(
 *         barWidthFraction = 0.6f,
 *         cornerRadius = CornerRadius.Large
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration - Solid for uniform bars, Gradient for horizontal gradient effect
 * @param barConfig Configuration for bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onBarClick Optional callback when a bar is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun HorizontalBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Horizontal bar chart data cannot be empty" }

    val (minValue, maxValue) = rememberHorizontalValueRange(dataList, barConfig.negativeValuesDrawMode)
    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val drawAxisAtZero = minValue < 0f && maxValue > 0f && isBelowAxisMode

    val animationProgress = rememberHorizontalAnimation(barConfig.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val barBounds = remember { mutableListOf<Pair<Rect, BarData>>() }
    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.map { it.label },
        yAxisConfig = createHorizontalAxisConfig(minValue, maxValue, drawAxisAtZero),
        config = scaffoldConfig,
        orientation = ChartOrientation.HORIZONTAL,
    ) { chartContext ->
        barBounds.clear()
        val axisOffset = calculateHorizontalAxisOffset(scaffoldConfig)
        val baselineX = calculateHorizontalBaselineX(drawAxisAtZero, minValue, maxValue, chartContext, axisOffset)

        drawHorizontalBars(
            HorizontalBarDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                barConfig = barConfig,
                baselineX = baselineX,
                axisOffset = axisOffset,
                animationProgress = animationProgress.value,
                color = color,
                isBelowAxisMode = isBelowAxisMode,
                minValue = minValue,
                maxValue = maxValue,
                onBarClick = onBarClick,
                onBarBoundCalculated = { barBounds.add(it) },
            )
        )

        drawHorizontalReferenceLineIfNeeded(barConfig, chartContext, textMeasurer)
        drawHorizontalTooltipIfNeeded(tooltipState, barConfig, textMeasurer, chartContext)
    }
}

