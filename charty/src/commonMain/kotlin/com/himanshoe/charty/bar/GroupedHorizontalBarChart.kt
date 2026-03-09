package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.GroupedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.GroupedHorizontalBarEntry
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.GroupedHorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.calculateGroupedHorizontalAxisOffset
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.calculateGroupedHorizontalBaselineX
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.createGroupedHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.createGroupedHorizontalBarChartModifier
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.drawGroupedHorizontalBars
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.drawGroupedHorizontalReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.drawGroupedHorizontalTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.rememberGroupedHorizontalState
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.rememberTooltipManager

/**
 * A composable that displays a **grouped horizontal bar chart**.
 *
 * Each [BarGroup] becomes one row of horizontally-oriented bars laid out side-by-side
 * vertically within the row. This is the horizontal counterpart to a grouped (clustered)
 * bar chart and is best suited for:
 * - Comparing multiple series across categories.
 * - Scenarios where category labels are long (horizontal layout accommodates them naturally).
 * - Data that includes negative values — bars extend to the left of the zero baseline.
 *
 * ### Colour resolution (per bar, in priority order)
 * 1. `BarGroup.colors[barIndex]` — per-bar override.
 * 2. `colors.value[barIndex % colorCount]` — chart-level palette cycle.
 *
 * ### Negative value support
 * - **[NegativeValuesDrawMode.BELOW_AXIS]** (default): positive bars grow right, negative
 *   bars grow left from a shared zero axis line. This is the most intuitive mode for data
 *   such as profit/loss or temperature anomalies.
 * - **[NegativeValuesDrawMode.FROM_MIN_VALUE]**: all bars start from the minimum value so
 *   the visual emphasis is on relative differences rather than absolute sign.
 *
 * @param data Lambda returning the list of [BarGroup] to display. Each group forms one bar row.
 * @param modifier Modifier applied to the chart container.
 * @param colors Colour palette for the bars. A [ChartyColor.Gradient] with one colour per
 *   bar-in-group gives the clearest series distinction. Defaults to [ChartyColors.ModernPalette].
 * @param config Appearance and behaviour settings — bar thickness, spacing between bars in a
 *   group, corner radius, negative-value mode, animation, reference line, and tooltip.
 * @param scaffoldConfig Chart scaffold configuration controlling axes, grid lines, and labels.
 * @param onBarClick Optional callback invoked when a bar is tapped, receiving a
 *   [GroupedHorizontalBarEntry] that describes the group, bar index, and value.
 *
 * Example usage:
 * ```kotlin
 * GroupedHorizontalBarChart(
 *     data = {
 *         listOf(
 *             BarGroup("North", listOf(120f, 85f, 60f)),
 *             BarGroup("South", listOf(95f, 110f, 75f)),
 *             BarGroup("East",  listOf(80f,  90f, 100f)),
 *             BarGroup("West",  listOf(105f, 70f, 80f)),
 *         )
 *     },
 *     colors = ChartyColors.ModernPalette,
 *     config = GroupedHorizontalBarChartConfig(
 *         barWidthFraction = 0.8f,
 *         barSpacing = 4f,
 *         cornerRadius = CornerRadius.Medium,
 *         animation = Animation.Default,
 *     ),
 *     onBarClick = { entry ->
 *         println("${entry.barGroup.label}[${entry.barIndex}] = ${entry.barValue}")
 *     },
 * )
 * ```
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun GroupedHorizontalBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColors.ModernPalette,
    config: GroupedHorizontalBarChartConfig = GroupedHorizontalBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((GroupedHorizontalBarEntry) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Grouped horizontal bar chart data cannot be empty" }
    require(dataList.all { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val (minValue, maxValue, colorList) = rememberGroupedHorizontalState(
        dataList = dataList,
        negativeValuesDrawMode = config.negativeValuesDrawMode,
        colors = colors,
    )

    val isBelowAxisMode = config.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val drawAxisAtZero = minValue < 0f && maxValue > 0f && isBelowAxisMode

    val animationProgress = rememberChartAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Rect, GroupedHorizontalBarEntry>()
    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier.then(
            createGroupedHorizontalBarChartModifier(
                dataList = dataList,
                config = config,
                onBarClick = onBarClick,
                barBounds = tooltipManager.bounds,
                onTooltipStateChange = tooltipManager::updateTooltip,
            ),
        ),
        xLabels = dataList.map { it.label },
        yAxisConfig = createGroupedHorizontalAxisConfig(minValue, maxValue, drawAxisAtZero),
        config = scaffoldConfig,
        orientation = ChartOrientation.HORIZONTAL,
    ) { chartContext ->
        tooltipManager.clearBounds()
        val axisOffset = calculateGroupedHorizontalAxisOffset(scaffoldConfig)
        val baselineX = calculateGroupedHorizontalBaselineX(
            drawAxisAtZero = drawAxisAtZero,
            minValue = minValue,
            maxValue = maxValue,
            chartContext = chartContext,
            axisOffset = axisOffset,
        )

        drawGroupedHorizontalBars(
            GroupedHorizontalBarDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                config = config,
                colorList = colorList,
                baselineX = baselineX,
                axisOffset = axisOffset,
                minValue = minValue,
                maxValue = maxValue,
                animationProgress = animationProgress.value,
                onBarClick = onBarClick,
                barBounds = tooltipManager.bounds,
            ),
        )

        drawGroupedHorizontalReferenceLineIfNeeded(config, chartContext, textMeasurer)
        drawGroupedHorizontalTooltipIfNeeded(tooltipManager.tooltipState, config, textMeasurer, chartContext)
    }
}
