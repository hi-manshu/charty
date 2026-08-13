package com.himanshoe.charty.bar.internal.bar.lollipop

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.SeriesCrosshairScope
import com.himanshoe.charty.bar.internal.bar.drawSeriesCrosshair
import com.himanshoe.charty.bar.internal.bar.rememberSeriesCrosshair
import com.himanshoe.charty.bar.internal.bar.verticalBarMarkerPositions
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.ChartCrosshair

/**
 * Builds the crosshair scope for [com.himanshoe.charty.bar.LollipopBarChart], which plots one value
 * per category up a vertical value axis and therefore snaps along x.
 *
 * @param config The chart's configuration, supplying the label text and any configured styling.
 * @param crosshair The crosshair passed to the chart, or `null` when none was.
 * @param interactionConfig Supplies the viewport or streaming geometry used for cross-chart syncing.
 * @return The scope the chart threads through its modifier, draw pass, and overlays.
 */
@Composable
internal fun rememberLollipopCrosshair(
    config: LollipopBarChartConfig,
    crosshair: ChartCrosshair<BarData>?,
    interactionConfig: ChartInteractionConfig,
): SeriesCrosshairScope<BarData> =
    rememberSeriesCrosshair(
        crosshairConfig = config.crosshairConfig,
        crosshair = crosshair,
        interactionConfig = interactionConfig,
        labelFormatter = config.tooltipFormatter,
        orientation = ChartOrientation.VERTICAL,
    )

/**
 * The crosshair anchor of every lollipop: the centre of its head, which is the same point the
 * persistent markers of [com.himanshoe.charty.bar.LollipopBarChart] are pinned to.
 *
 * @param chartContext The chart's coordinate context.
 * @param values The drawn value of each lollipop, ordered left to right.
 * @return One [Offset] per value.
 */
internal fun lollipopCrosshairAnchors(
    chartContext: ChartContext,
    values: List<Float>,
): List<Offset> = verticalBarMarkerPositions(chartContext = chartContext, values = values)

/**
 * Refreshes the crosshair's anchors from the lollipops drawn this frame and draws the guide line,
 * highlight dot, and label. Does nothing when the chart has no crosshair.
 *
 * @param crosshair The chart's crosshair scope.
 * @param dataList The lollipops the crosshair reports, in drawn order.
 * @param displayList The same lollipops carrying the values actually drawn this frame.
 * @param chartContext The chart's coordinate context.
 * @param config The chart's configuration, supplying the head colour override.
 * @param colors The chart's colour, used to fill the highlight dot when no head colour is set.
 * @param textMeasurer Measures the label.
 */
internal fun DrawScope.drawLollipopCrosshair(
    crosshair: SeriesCrosshairScope<BarData>,
    dataList: List<BarData>,
    displayList: List<BarData>,
    chartContext: ChartContext,
    config: LollipopBarChartConfig,
    colors: ChartyColor,
    textMeasurer: TextMeasurer,
) {
    if (!crosshair.isActive) {
        return
    }
    drawSeriesCrosshair(
        crosshair = crosshair,
        anchors =
            lollipopCrosshairAnchors(
                chartContext = chartContext,
                values = displayList.fastMap { it.value },
            ),
        items = dataList,
        chartContext = chartContext,
        color = config.circleColor ?: colors,
        textMeasurer = textMeasurer,
    )
}
