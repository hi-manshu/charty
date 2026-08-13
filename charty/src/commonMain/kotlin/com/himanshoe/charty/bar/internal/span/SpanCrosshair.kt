package com.himanshoe.charty.bar.internal.span

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.bar.internal.bar.SeriesCrosshairScope
import com.himanshoe.charty.bar.internal.bar.drawSeriesCrosshair
import com.himanshoe.charty.bar.internal.bar.rememberSeriesCrosshair
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.util.toChartLabel

/**
 * The label a span's crosshair reads: the same `label: startValue - endValue` text its tap tooltip
 * shows, because a span has a start and an end rather than the single value
 * [BarChartConfig.tooltipFormatter] would receive.
 *
 * @param spanData The span under the crosshair.
 * @return The text drawn in the crosshair label.
 */
internal fun formatSpanCrosshairLabel(spanData: SpanData): String =
    "${spanData.label}: ${spanData.startValue.toChartLabel()} - ${spanData.endValue.toChartLabel()}"

/**
 * Builds the crosshair scope for [com.himanshoe.charty.bar.SpanChart]. The chart stacks its
 * categories down the plot and runs values left to right, so the crosshair snaps along y and its
 * guide line is horizontal.
 *
 * @param config The chart's configuration, supplying any configured styling.
 * @param crosshair The crosshair passed to the chart, or `null` when none was.
 * @param interactionConfig Supplies the viewport or streaming geometry used for cross-chart syncing.
 * @return The scope the chart threads through its modifier, draw pass, and overlays.
 */
@Composable
internal fun rememberSpanCrosshair(
    config: BarChartConfig,
    crosshair: ChartCrosshair<SpanData>?,
    interactionConfig: ChartInteractionConfig,
): SeriesCrosshairScope<SpanData> =
    rememberSeriesCrosshair(
        crosshairConfig = config.crosshairConfig,
        crosshair = crosshair,
        interactionConfig = interactionConfig,
        labelFormatter = ::formatSpanCrosshairLabel,
        orientation = ChartOrientation.HORIZONTAL,
    )

/**
 * The crosshair anchor of every span: the centre of its end edge, the same point the chart's
 * persistent markers are pinned to.
 *
 * @param chartContext The chart's coordinate context.
 * @param endValues The end value of each drawn span, ordered top to bottom.
 * @param minValue The value at the left edge of the plot.
 * @param maxValue The value at the right edge of the plot.
 * @param axisOffset The left inset reserved for the axis.
 * @return One [Offset] per span.
 */
internal fun spanCrosshairAnchors(
    chartContext: ChartContext,
    endValues: List<Float>,
    minValue: Float,
    maxValue: Float,
    axisOffset: Float,
): List<Offset> =
    spanEndMarkerPositions(
        chartContext = chartContext,
        endValues = endValues,
        minValue = minValue,
        maxValue = maxValue,
        axisOffset = axisOffset,
    )

/**
 * Refreshes the crosshair's anchors from the spans drawn this frame and draws the guide line,
 * highlight dot, and label. Does nothing when the chart has no crosshair.
 *
 * @param crosshair The chart's crosshair scope.
 * @param params The draw parameters of the spans this frame, supplying geometry and colours.
 * @param dataList The spans the crosshair reports, in drawn order.
 * @param textMeasurer Measures the label.
 */
internal fun DrawScope.drawSpanCrosshair(
    crosshair: SeriesCrosshairScope<SpanData>,
    params: SpanDrawParams,
    dataList: List<SpanData>,
    textMeasurer: TextMeasurer,
) {
    if (!crosshair.isActive) {
        return
    }
    drawSeriesCrosshair(
        crosshair = crosshair,
        anchors =
            spanCrosshairAnchors(
                chartContext = params.chartContext,
                endValues = params.dataList.fastMap { it.endValue },
                minValue = params.minValue,
                maxValue = params.maxValue,
                axisOffset = params.axisOffset,
            ),
        items = dataList,
        chartContext = params.chartContext,
        color = spanCrosshairDotColor(selected = crosshair.manager?.selectedItem, colors = params.colors),
        textMeasurer = textMeasurer,
    )
}

private fun spanCrosshairDotColor(
    selected: SpanData?,
    colors: ChartyColor,
): ChartyColor = selected?.color ?: colors
