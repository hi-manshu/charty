package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.stacked.StackedBarDrawParams
import com.himanshoe.charty.bar.internal.bar.stacked.createStackedBarChartModifier
import com.himanshoe.charty.bar.internal.bar.stacked.drawStackedBars
import com.himanshoe.charty.bar.internal.bar.stacked.drawStackedReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.stacked.drawStackedTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.stacked.rememberStackedMaxTotal
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.rememberTooltipManager

/**
 * Stacked Bar Chart - Display data as stacked vertical bars showing composition
 *
 * A stacked bar chart shows multiple values stacked on top of each other,
 * displaying both individual values and the total. Useful for showing part-to-whole
 * relationships and composition over categories.
 *
 * Usage:
 * ```kotlin
 * StackedBarChart(
 *     data = {
 *         listOf(
 *             BarGroup("Q1", listOf(20f, 30f, 15f)),
 *             BarGroup("Q2", listOf(25f, 35f, 20f)),
 *             BarGroup("Q3", listOf(30f, 25f, 25f))
 *         )
 *     },
 *     colors = ChartyColors.DefaultGradient,
 *     stackedConfig = StackedBarChartConfig(
 *         barWidthFraction = 0.7f,
 *         topCornerRadius = CornerRadius.Medium
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar groups (each group represents one stacked bar)
 * @param modifier Modifier for the chart
 * @param colors Color configuration - Gradient assigns different color to each stack segment
 * @param stackedConfig Configuration for stacked bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun StackedBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColors.DefaultGradient,
    stackedConfig: StackedBarChartConfig = StackedBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onSegmentClick: ((StackedBarSegment) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Stacked bar chart data cannot be empty" }
    require(dataList.all { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val (maxTotal, colorList) = rememberStackedMaxTotal(dataList, colors)
    val animationProgress = rememberChartAnimation(stackedConfig.animation)
    val tooltipManager = rememberTooltipManager<Rect, StackedBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier.then(
            createStackedBarChartModifier(
                dataList = dataList,
                stackedConfig = stackedConfig,
                onSegmentClick = onSegmentClick,
                segmentBounds = tooltipManager.bounds,
                onTooltipStateChange = tooltipManager::updateTooltip
            )
        ),
        xLabels = dataList.map { it.label },
        yAxisConfig =
            AxisConfig(
                minValue = 0f,
                maxValue = maxTotal,
                steps = 6,
            ),
        config = scaffoldConfig,
    ) { chartContext ->
        tooltipManager.clearBounds()

        drawStackedBars(
            StackedBarDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                stackedConfig = stackedConfig,
                colorList = colorList,
                animationProgress = animationProgress.value,
                onSegmentClick = onSegmentClick,
                segmentBounds = tooltipManager.bounds,
            )
        )

        drawStackedReferenceLineIfNeeded(stackedConfig, chartContext, textMeasurer)
        drawStackedTooltipIfNeeded(tooltipManager.tooltipState, stackedConfig, textMeasurer, chartContext)
    }
}
