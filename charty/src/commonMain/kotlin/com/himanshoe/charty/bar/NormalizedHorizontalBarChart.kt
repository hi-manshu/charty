package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.NormalizedHorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.calculateNormalizedHorizontalAxisOffset
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.createNormalizedHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.createNormalizedHorizontalBarChartModifier
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.drawNormalizedHorizontalBars
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.drawNormalizedHorizontalTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.rememberNormalizedHorizontalColors
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.rememberTooltipManager

/**
 * A composable that displays a **normalized (100%) horizontal bar chart**.
 *
 * Each [BarGroup] is rendered as a single horizontal bar that always extends to 100% of the
 * available width. Segments are sized proportionally to each value's share of the group total,
 * making part-to-whole relationships immediately comparable across all rows.
 *
 * This is the horizontal counterpart of [MosiacBarChart] and is ideal for:
 * - Survey results (e.g. "strongly agree / agree / neutral / disagree").
 * - Budget allocation breakdowns across departments.
 * - Any comparison where relative proportion matters more than absolute magnitude.
 *
 * ### Colour resolution (per segment, in priority order)
 * 1. `BarGroup.colors[segmentIndex]` — per-segment override.
 * 2. `colors.value[segmentIndex % colorCount]` — chart-level palette cycle.
 *
 * ### Negative values
 * Values ≤ 0 are silently excluded from normalization. All positive values within a group
 * are proportionally distributed across the full bar width. If you need to display negatives
 * consider [GroupedHorizontalBarChart] or [HorizontalBarChart] instead.
 *
 * @param data Lambda returning the list of [BarGroup] to display. Each group becomes one bar row.
 * @param modifier Modifier applied to the chart container.
 * @param colors Colour palette for segments. A [ChartyColor.Gradient] with one colour per
 *   segment gives the clearest distinction. Defaults to [ChartyColors.DefaultGradient].
 * @param config Appearance and behaviour settings — bar thickness, corner radius, animation,
 *   and tooltip configuration.
 * @param scaffoldConfig Chart scaffold configuration controlling axes, grid lines, and labels.
 * @param onSegmentClick Optional callback invoked when a segment is tapped, receiving a
 *   [NormalizedHorizontalBarSegment] with the group, segment index, raw value, and percentage.
 *
 * Example usage:
 * ```kotlin
 * NormalizedHorizontalBarChart(
 *     data = {
 *         listOf(
 *             BarGroup("Strongly Agree",    listOf(40f, 35f, 15f, 10f)),
 *             BarGroup("Agree",             listOf(30f, 40f, 20f, 10f)),
 *             BarGroup("Neutral",           listOf(20f, 25f, 35f, 20f)),
 *             BarGroup("Disagree",          listOf(10f, 15f, 30f, 45f)),
 *         )
 *     },
 *     colors = ChartyColors.DefaultGradient,
 *     config = NormalizedHorizontalBarChartConfig(
 *         barWidthFraction = 0.6f,
 *         rightCornerRadius = CornerRadius.Medium,
 *         animation = Animation.Default,
 *     ),
 *     onSegmentClick = { segment ->
 *         println("${segment.barGroup.label}: ${segment.segmentPercentage.toInt()}%")
 *     },
 * )
 * ```
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun NormalizedHorizontalBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColors.DefaultGradient,
    config: NormalizedHorizontalBarChartConfig = NormalizedHorizontalBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onSegmentClick: ((NormalizedHorizontalBarSegment) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Normalized horizontal bar chart data cannot be empty" }
    require(dataList.all { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val colorList = rememberNormalizedHorizontalColors(dataList, colors)
    val animationProgress = rememberChartAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Rect, NormalizedHorizontalBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier.then(
            createNormalizedHorizontalBarChartModifier(
                dataList = dataList,
                config = config,
                onSegmentClick = onSegmentClick,
                segmentBounds = tooltipManager.bounds,
                onTooltipStateChange = tooltipManager::updateTooltip,
            ),
        ),
        xLabels = dataList.map { it.label },
        yAxisConfig = createNormalizedHorizontalAxisConfig(),
        config = scaffoldConfig,
        orientation = ChartOrientation.HORIZONTAL,
    ) { chartContext ->
        tooltipManager.clearBounds()
        val axisOffset = calculateNormalizedHorizontalAxisOffset(scaffoldConfig)

        drawNormalizedHorizontalBars(
            NormalizedHorizontalBarDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                config = config,
                colorList = colorList,
                axisOffset = axisOffset,
                animationProgress = animationProgress.value,
                onSegmentClick = onSegmentClick,
                segmentBounds = tooltipManager.bounds,
            ),
        )

        drawNormalizedHorizontalTooltipIfNeeded(tooltipManager.tooltipState, config, textMeasurer, chartContext)
    }
}
