package com.himanshoe.charty.bar.internal.bar.comparison

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.SeriesCrosshairScope
import com.himanshoe.charty.bar.internal.bar.drawSeriesCrosshair
import com.himanshoe.charty.bar.internal.bar.rememberSeriesCrosshair
import com.himanshoe.charty.bar.internal.bar.verticalBarMarkerPositions
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.ChartCrosshair

private val COMPARISON_CROSSHAIR_FALLBACK_COLOR = ChartyColor.Solid(Color.Gray)

/**
 * Builds the crosshair scope for [com.himanshoe.charty.bar.ComparisonBarChart], whose groups run left
 * to right along a category axis and therefore snap along x.
 *
 * @param config The chart's configuration, supplying the label text and any configured styling.
 * @param crosshair The crosshair passed to the chart, or `null` when none was.
 * @param interactionConfig Supplies the viewport or streaming geometry used for cross-chart syncing.
 * @return The scope the chart threads through its modifier, draw pass, and overlays.
 */
@Composable
internal fun rememberComparisonCrosshair(
    config: ComparisonBarChartConfig,
    crosshair: ChartCrosshair<ComparisonBarSegment>?,
    interactionConfig: ChartInteractionConfig,
): SeriesCrosshairScope<ComparisonBarSegment> =
    rememberSeriesCrosshair(
        crosshairConfig = config.crosshairConfig,
        crosshair = crosshair,
        interactionConfig = interactionConfig,
        labelFormatter = config.tooltipFormatter,
        orientation = ChartOrientation.VERTICAL,
    )

/**
 * The crosshair anchor of every comparison group: the top-centre of the group, level with its tallest
 * bar. This is the anchor the chart's persistent markers already use, so one guide serves the whole
 * group rather than jumping between the bars inside it.
 *
 * @param chartContext The chart's coordinate context.
 * @param dataList The drawn groups, ordered left to right.
 * @return One [Offset] per group.
 */
internal fun comparisonCrosshairAnchors(
    chartContext: ChartContext,
    dataList: List<BarGroup>,
): List<Offset> =
    verticalBarMarkerPositions(
        chartContext = chartContext,
        values = dataList.fastMap { group -> group.values.maxOrNull() ?: 0f },
    )

/**
 * The segment the crosshair reports for every group: the group's tallest bar, which is the bar its
 * anchor rests on. When several bars tie for tallest the earliest one is reported.
 *
 * @param dataList The groups the crosshair reports, in drawn order.
 * @return One [ComparisonBarSegment] per group.
 */
internal fun comparisonCrosshairSegments(dataList: List<BarGroup>): List<ComparisonBarSegment> =
    dataList.fastMap { group ->
        val tallest = group.values.maxOrNull() ?: 0f
        val barIndex = group.values.indexOf(tallest).coerceAtLeast(0)
        ComparisonBarSegment(
            barGroup = group,
            barIndex = barIndex,
            barValue = group.values.getOrElse(barIndex) { 0f },
        )
    }

/**
 * Refreshes the crosshair's anchors from the groups drawn this frame and draws the guide line,
 * highlight dot, and label. The dot takes the colour of the bar it rests on. Does nothing when the
 * chart has no crosshair.
 *
 * @param crosshair The chart's crosshair scope.
 * @param dataList The groups the crosshair reports, in drawn order.
 * @param displayList The same groups carrying the values actually drawn this frame.
 * @param chartContext The chart's coordinate context.
 * @param textMeasurer Measures the label.
 */
internal fun DrawScope.drawComparisonCrosshair(
    crosshair: SeriesCrosshairScope<ComparisonBarSegment>,
    dataList: List<BarGroup>,
    displayList: List<BarGroup>,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
) {
    if (!crosshair.isActive) {
        return
    }
    drawSeriesCrosshair(
        crosshair = crosshair,
        anchors = comparisonCrosshairAnchors(chartContext = chartContext, dataList = displayList),
        items = comparisonCrosshairSegments(dataList),
        chartContext = chartContext,
        color = comparisonCrosshairDotColor(selected = crosshair.manager?.selectedItem),
        textMeasurer = textMeasurer,
    )
}

private fun comparisonCrosshairDotColor(selected: ComparisonBarSegment?): ChartyColor =
    selected?.let { segment -> segment.barGroup.colors?.getOrNull(segment.barIndex) }
        ?: COMPARISON_CROSSHAIR_FALLBACK_COLOR
