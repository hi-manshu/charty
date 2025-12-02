package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
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
import com.himanshoe.charty.bar.internal.bar.stacked.rememberStackedAnimation
import com.himanshoe.charty.bar.internal.bar.stacked.rememberStackedMaxTotal
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.TooltipState

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
 *     colors = ChartyColor.Gradient(
 *         listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800))
 *     ),
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
    colors: ChartyColor =
        ChartyColor.Gradient(
            listOf(
                Color(0xFF2196F3),
                Color(0xFF4CAF50),
                Color(0xFFFF9800),
            ),
        ),
    stackedConfig: StackedBarChartConfig = StackedBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onSegmentClick: ((StackedBarSegment) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Stacked bar chart data cannot be empty" }
    require(dataList.all { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val (maxTotal, colorList) = rememberStackedMaxTotal(dataList, colors)
    val animationProgress = rememberStackedAnimation(stackedConfig.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val segmentBounds = remember { mutableListOf<Pair<Rect, StackedBarSegment>>() }
    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier.then(
            createStackedBarChartModifier(
                dataList = dataList,
                stackedConfig = stackedConfig,
                onSegmentClick = onSegmentClick,
                segmentBounds = segmentBounds,
                onTooltipStateChange = { tooltipState = it }
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
        segmentBounds.clear()

        drawStackedBars(
            StackedBarDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                stackedConfig = stackedConfig,
                colorList = colorList,
                animationProgress = animationProgress.value,
                onSegmentClick = onSegmentClick,
                segmentBounds = segmentBounds,
            )
        )

        drawStackedReferenceLineIfNeeded(stackedConfig, chartContext, textMeasurer)
        drawStackedTooltipIfNeeded(tooltipState, stackedConfig, textMeasurer, chartContext)
    }
}
