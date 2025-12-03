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
import com.himanshoe.charty.bar.internal.bar.barchart.calculateBarBaselineY
import com.himanshoe.charty.bar.internal.bar.barchart.createBarAxisConfig
import com.himanshoe.charty.bar.internal.bar.barchart.createBarChartModifier
import com.himanshoe.charty.bar.internal.bar.barchart.drawBarReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.barchart.drawBarTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.barchart.drawBars
import com.himanshoe.charty.bar.internal.bar.barchart.rememberBarAnimationProgress
import com.himanshoe.charty.bar.internal.bar.barchart.rememberBarValueRange
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Bar Chart - Display data as vertical bars
 *
 * A bar chart presents categorical data with rectangular bars. The height of each bar
 * is proportional to the value it represents. Ideal for comparing discrete categories.
 *
 * Usage:
 * ```kotlin
 * BarChart(
 *     data = {
 *         listOf(
 *             BarData("Jan", 100f),
 *             BarData("Feb", 150f),
 *             BarData("Mar", 120f)
 *         )
 *     },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 *     barConfig = BarChartConfig(
 *         barWidthFraction = 0.6f,
 *         roundedTopCorners = true,
 *         topCornerRadius = CornerRadius.Large,
 *         animation = Animation.Enabled()
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration - Solid for uniform bars, Gradient for vertical gradient effect
 * @param barConfig Configuration for bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels (includes leftLabelRotation)
 * @param onBarClick Optional callback when a bar is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun BarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Bar chart data cannot be empty" }

    val (minValue, maxValue) = rememberBarValueRange(dataList, barConfig.negativeValuesDrawMode)
    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = rememberBarAnimationProgress(barConfig.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val barBounds = remember { mutableListOf<Pair<Rect, BarData>>() }
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createBarChartModifier(
        modifier = modifier,
        onBarClick = onBarClick,
        dataList = dataList,
        barConfig = barConfig,
        barBounds = barBounds,
        onTooltipUpdate = { tooltipState = it },
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createBarAxisConfig(minValue, maxValue, isBelowAxisMode),
        config = scaffoldConfig,
        leftLabelRotation = scaffoldConfig.leftLabelRotation,
    ) { chartContext ->
        barBounds.clear()
        val baselineY = calculateBarBaselineY(minValue, isBelowAxisMode, chartContext)

        drawBars(
            dataList = dataList,
            chartContext = chartContext,
            barConfig = barConfig,
            baselineY = baselineY,
            animationProgress = animationProgress.value,
            color = color,
            onBarClick = onBarClick,
            barBounds = barBounds,
        )

        drawBarReferenceLineIfNeeded(barConfig, chartContext, textMeasurer)
        drawBarTooltipIfNeeded(tooltipState, barConfig, textMeasurer, chartContext)
    }
}
