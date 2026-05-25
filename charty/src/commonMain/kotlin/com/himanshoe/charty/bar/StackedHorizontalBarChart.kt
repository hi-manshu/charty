package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.StackedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.StackedHorizontalBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.StackedHorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.createStackedHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.createStackedHorizontalBarChartModifier
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.drawStackedHorizontalBars
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.drawStackedHorizontalReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.drawStackedHorizontalTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.rememberStackedHorizontalMaxTotal
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.rememberTooltipManager

/**
 * A composable that displays a **stacked horizontal bar chart**.
 *
 * Each [BarGroup] is rendered as a single horizontal bar split into colour-coded
 * segments — one segment per value in the group. Bars grow left-to-right with a
 * smooth entrance animation; all segments scale uniformly so the bar reads as a
 * cohesive unit rather than independent pieces.
 *
 * This chart is the horizontal counterpart to [StackedBarChart] and is ideal for:
 * - Part-to-whole comparisons across multiple categories.
 * - Situations where category labels are long (horizontal layout gives them more space).
 * - Survey results, budget breakdowns, or time-allocation visualisations.
 *
 * ### Colour resolution (per segment, in priority order)
 * 1. `BarGroup.colors[segmentIndex]` — per-segment override.
 * 2. `colors.value[segmentIndex % colorCount]` — chart-level palette cycle.
 *
 * ### Constraints
 * - All segment values must be **non-negative**. Negative stacked values are not supported.
 * - Every group must contain **at least one** value.
 *
 * @param data Lambda returning the list of [BarGroup] to display. Each group becomes one bar row.
 * @param modifier Modifier applied to the chart container.
 * @param colors Colour palette used to differentiate segments. A [ChartyColor.Gradient] with
 *   one colour per segment gives the clearest distinction. Defaults to [ChartyColors.DefaultGradient].
 * @param config Appearance and behaviour configuration — bar thickness, corner radius,
 *   animation, reference line, and tooltip settings.
 * @param scaffoldConfig Chart scaffold configuration controlling axes, grid lines, and labels.
 * @param onSegmentClick Optional callback invoked when a segment is tapped, receiving a
 *   [StackedHorizontalBarSegment] that describes the tapped bar group, segment index, and value.
 *
 * Example usage:
 * ```kotlin
 * StackedHorizontalBarChart(
 *     data = {
 *         listOf(
 *             BarGroup("Q1", listOf(20f, 30f, 15f)),
 *             BarGroup("Q2", listOf(25f, 35f, 20f)),
 *             BarGroup("Q3", listOf(30f, 25f, 25f)),
 *             BarGroup("Q4", listOf(28f, 32f, 18f)),
 *         )
 *     },
 *     colors = ChartyColors.DefaultGradient,
 *     config = StackedHorizontalBarChartConfig(
 *         barWidthFraction = 0.6f,
 *         rightCornerRadius = CornerRadius.Medium,
 *         animation = Animation.Default,
 *     ),
 *     onSegmentClick = { segment ->
 *         println("${segment.barGroup.label}[${segment.segmentIndex}] = ${segment.segmentValue}")
 *     },
 * )
 * ```
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun StackedHorizontalBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColors.DefaultGradient,
    config: StackedHorizontalBarChartConfig = StackedHorizontalBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onSegmentClick: ((StackedHorizontalBarSegment) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Stacked horizontal bar chart data cannot be empty" }
    require(dataList.all { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }
    require(dataList.all { group -> group.values.all { it >= 0f } }) {
        "Stacked horizontal bar chart does not support negative values"
    }

    val (maxTotal, colorList) = rememberStackedHorizontalMaxTotal(dataList, colors)
    val animationProgress = rememberChartAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Rect, StackedHorizontalBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier.then(
            createStackedHorizontalBarChartModifier(
                dataList = dataList,
                config = config,
                onSegmentClick = onSegmentClick,
                segmentBounds = tooltipManager.bounds,
                onTooltipStateChange = tooltipManager::updateTooltip,
            ),
        ),
        xLabels = dataList.map { it.label },
        yAxisConfig = createStackedHorizontalAxisConfig(maxTotal),
        config = scaffoldConfig,
        orientation = ChartOrientation.HORIZONTAL,
    ) { chartContext ->
        tooltipManager.clearBounds()

        drawStackedHorizontalBars(
            StackedHorizontalBarDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                config = config,
                colorList = colorList,
                maxTotal = maxTotal,
                animationProgress = animationProgress.value,
                onSegmentClick = onSegmentClick,
                segmentBounds = tooltipManager.bounds,
            ),
        )

        drawStackedHorizontalReferenceLineIfNeeded(config, chartContext, textMeasurer)
        drawStackedHorizontalTooltipIfNeeded(tooltipManager.tooltipState, config, textMeasurer, chartContext)
    }
}
