package com.himanshoe.charty.bar.internal.bar

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.formatMarkerValue

private const val CENTER_DIVISOR = 2f

/**
 * Draws [markers] on a vertical bar chart, anchored at the top-centre of each bar. Nothing is drawn
 * when there are no markers or no [textMeasurer] to lay out their labels.
 *
 * @param chartContext The chart's coordinate context.
 * @param markers The markers to draw.
 * @param values The value of each drawn bar, ordered left to right.
 * @param textMeasurer Measures the callout labels.
 */
internal fun DrawScope.drawVerticalBarMarkers(
    chartContext: ChartContext,
    markers: List<PersistentMarker>,
    values: List<Float>,
    textMeasurer: TextMeasurer?,
) {
    if (markers.isEmpty() || textMeasurer == null) {
        return
    }
    drawPersistentMarkers(
        chartContext = chartContext,
        markers = markers,
        pointPositions = verticalBarMarkerPositions(chartContext = chartContext, values = values),
        valueLabelFor = { index -> formatMarkerValue(values[index]) },
        textMeasurer = textMeasurer,
    )
}

/**
 * Draws [markers] on a horizontal bar chart, anchored at the centre of each bar's value end. Nothing
 * is drawn when there are no markers or no [textMeasurer] to lay out their labels.
 *
 * @param chartContext The chart's coordinate context.
 * @param markers The markers to draw.
 * @param values The value of each drawn bar, ordered top to bottom.
 * @param minValue The value at the left edge of the plot.
 * @param maxValue The value at the right edge of the plot.
 * @param textMeasurer Measures the callout labels.
 */
internal fun DrawScope.drawHorizontalBarMarkers(
    chartContext: ChartContext,
    markers: List<PersistentMarker>,
    values: List<Float>,
    minValue: Float,
    maxValue: Float,
    textMeasurer: TextMeasurer?,
) {
    if (markers.isEmpty() || textMeasurer == null) {
        return
    }
    drawPersistentMarkers(
        chartContext = chartContext,
        markers = markers,
        pointPositions =
            horizontalBarMarkerPositions(
                chartContext = chartContext,
                values = values,
                minValue = minValue,
                maxValue = maxValue,
            ),
        valueLabelFor = { index -> formatMarkerValue(values[index]) },
        textMeasurer = textMeasurer,
    )
}

/**
 * Anchor points for the persistent markers of a vertical bar chart: the top-centre of each bar, in
 * the same order as the drawn data, so a marker index of `-1` lands on the rightmost bar.
 *
 * @param chartContext The chart's coordinate context.
 * @param values The value of each drawn bar, ordered left to right.
 * @return One [Offset] per value, at the centre of the bar's top edge.
 */
internal fun verticalBarMarkerPositions(
    chartContext: ChartContext,
    values: List<Float>,
): List<Offset> =
    List(values.size) { index ->
        Offset(
            x = chartContext.calculateCenteredXPosition(index = index, totalItems = values.size),
            y = chartContext.convertValueToYPosition(values[index]),
        )
    }

/**
 * Anchor points for the persistent markers of a horizontal bar chart: the centre of each bar's value
 * end (not its axis end), in the same order as the drawn data, so a marker index of `-1` lands on
 * the bottom bar.
 *
 * @param chartContext The chart's coordinate context.
 * @param values The value of each drawn bar, ordered top to bottom.
 * @param minValue The value at the left edge of the plot.
 * @param maxValue The value at the right edge of the plot.
 * @return One [Offset] per value, at the centre of the bar's value end.
 */
internal fun horizontalBarMarkerPositions(
    chartContext: ChartContext,
    values: List<Float>,
    minValue: Float,
    maxValue: Float,
): List<Offset> {
    val range = maxValue - minValue
    val slotHeight = chartContext.calculateSlotHeight(values.size)
    return List(values.size) { index ->
        val normalized =
            if (range == 0f) {
                0f
            } else {
                (values[index] - minValue) / range
            }
        Offset(
            x = chartContext.left + normalized * chartContext.width,
            y =
                chartContext.calculateSlotTopPosition(index = index, totalItems = values.size) +
                    slotHeight / CENTER_DIVISOR,
        )
    }
}

/**
 * Replaces [bounds] with one anchor per drawn bar, paired with the data item it belongs to.
 *
 * @param bounds The list to refill; cleared first.
 * @param dataList The bars whose labels and values the crosshair reports.
 * @param values The drawn value of each bar, in the same order as [dataList].
 * @param chartContext The chart's coordinate context.
 * @param orientation The chart's orientation, deciding which anchor geometry is used.
 */
internal fun recordBarCrosshairBounds(
    bounds: MutableList<Pair<Offset, BarData>>,
    dataList: List<BarData>,
    values: List<Float>,
    chartContext: ChartContext,
    orientation: ChartOrientation,
) {
    bounds.clear()
    val anchors = barCrosshairAnchors(chartContext = chartContext, values = values, orientation = orientation)
    val count = minOf(anchors.size, dataList.size)
    for (index in 0 until count) {
        bounds.add(anchors[index] to dataList[index])
    }
}

/**
 * The crosshair anchor point of every bar: the top centre for a [ChartOrientation.VERTICAL] chart and
 * the centre of the bar's value end for a [ChartOrientation.HORIZONTAL] one.
 *
 * @param chartContext The chart's coordinate context.
 * @param values The drawn value of each bar, in drawn order.
 * @param orientation The chart's orientation.
 * @return One [Offset] per value.
 */
internal fun barCrosshairAnchors(
    chartContext: ChartContext,
    values: List<Float>,
    orientation: ChartOrientation,
): List<Offset> =
    when (orientation) {
        ChartOrientation.VERTICAL -> verticalBarMarkerPositions(chartContext = chartContext, values = values)
        ChartOrientation.HORIZONTAL ->
            horizontalBarMarkerPositions(
                chartContext = chartContext,
                values = values,
                minValue = chartContext.minValue,
                maxValue = chartContext.maxValue,
            )
    }
