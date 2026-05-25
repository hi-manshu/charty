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
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastMap

/**
 * A composable that displays a **normalized (100%) horizontal bar chart**.
 *
 * Each [BarGroup] is rendered as a single horizontal bar that always extends to 100% of the
 * available width. Segments are sized proportionally to each value's share of the group total.
 *
 * @param data Lambda returning the list of [BarGroup] to display. Each group becomes one bar row.
 * @param modifier Modifier applied to the chart container.
 * @param colors Colour palette for segments.
 * @param config Appearance and behaviour settings — bar thickness, corner radius, animation,
 *   and tooltip configuration.
 * @param scaffoldConfig Chart scaffold configuration controlling axes, grid lines, and labels.
 * @param onSegmentClick Optional callback invoked when a segment is tapped.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 *
 * Example usage:
 * ```kotlin
 * NormalizedHorizontalBarChart(
 *     data = {
 *         listOf(
 *             BarGroup("Strongly Agree", listOf(40f, 35f, 15f, 10f)),
 *             BarGroup("Agree",          listOf(30f, 40f, 20f, 10f)),
 *         )
 *     },
 *     colors = ChartyColors.DefaultGradient,
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
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Normalized horizontal bar chart data cannot be empty" }
    require(fullDataList.fastAll { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val colorList = rememberNormalizedHorizontalColors(dataList, colors)
    val animationProgress = rememberChartAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Rect, NormalizedHorizontalBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier = createNormalizedHorizontalBarChartModifier(
        dataList = dataList,
        config = config,
        onSegmentClick = onSegmentClick,
        segmentBounds = tooltipManager.bounds,
        onTooltipStateChange = tooltipManager::updateTooltip,
    )

    val chartModifier = buildInteractionModifier(
        base = modifier.then(clickModifier),
        interactionConfig = interactionConfig,
        dataList = dataList,
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.fastMap { it.label },
        yAxisConfig = createNormalizedHorizontalAxisConfig(),
        config = scaffoldConfig,
        orientation = ChartOrientation.HORIZONTAL,
        contentDescription = interactionConfig.accessibilityDescription
            ?: "Normalized horizontal bar chart, ${fullDataList.size} data points.",
    ) { chartContext ->
        updateInteractionBounds(interactionConfig, chartContext)

        tooltipManager.clearBounds()

        drawNormalizedHorizontalBars(
            NormalizedHorizontalBarDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                config = config,
                colorList = colorList,
                animationProgress = animationProgress.value,
                onSegmentClick = onSegmentClick,
                segmentBounds = tooltipManager.bounds,
            ),
        )

        drawNormalizedHorizontalTooltipIfNeeded(tooltipManager.tooltipState, config, textMeasurer, chartContext)

        drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)
    }
}
