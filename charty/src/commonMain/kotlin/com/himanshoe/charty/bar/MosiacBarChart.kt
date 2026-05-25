package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.MosiacBarChartConfig
import com.himanshoe.charty.bar.config.MosiacBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.mosiac.createMosiacAxisConfig
import com.himanshoe.charty.bar.internal.bar.mosiac.createMosiacChartModifier
import com.himanshoe.charty.bar.internal.bar.mosiac.drawMosiacBars
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.tooltip.rememberTooltipManager

/**
 * Mosiac Bar Chart - 100% stacked bar chart.
 *
 * Each bar represents a category whose segments are normalized to 100% of
 * the bar height, similar to a mosaic / 100% stacked bar chart.
 *
 * @param data Lambda returning list of bar groups to display
 * @param modifier Modifier for the chart
 * @param config Configuration for mosiac chart appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onSegmentClick Optional callback when a segment is clicked
 */
@Composable
fun MosiacBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    config: MosiacBarChartConfig = MosiacBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onSegmentClick: ((MosiacBarSegment) -> Unit)? = null,
) {
    val groups = remember(data) { data() }
    require(groups.isNotEmpty()) { "Mosiac bar chart data cannot be empty" }
    require(groups.all { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val animationProgress = rememberChartAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Rect, MosiacBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createMosiacChartModifier(
        modifier = modifier,
        onSegmentClick = onSegmentClick,
        groups = groups,
        config = config,
        segmentBounds = tooltipManager.bounds,
        onTooltipUpdate = tooltipManager::updateTooltip,
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = groups.map { it.label },
        yAxisConfig = createMosiacAxisConfig(),
        config = scaffoldConfig,
    ) { chartContext ->
        tooltipManager.clearBounds()

        drawMosiacBars(
            groups = groups,
            chartContext = chartContext,
            config = config,
            animationProgress = animationProgress.value,
            onSegmentClick = onSegmentClick,
            onSegmentBoundCalculated = { tooltipManager.bounds.add(it) },
        )

        tooltipManager.tooltipState?.let { state ->
            drawTooltip(
                tooltipState = state,
                config = config.tooltipConfig,
                textMeasurer = textMeasurer,
                chartWidth = chartContext.right,
                chartTop = chartContext.top,
                chartBottom = chartContext.bottom,
            )
        }
    }
}
