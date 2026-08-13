package com.himanshoe.charty.bar.internal.bar.waterfall

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.bar.config.WaterfallChartConfig
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
 * Builds the crosshair scope for [com.himanshoe.charty.bar.WaterfallChart], whose steps run left to
 * right along a category axis and therefore snap along x.
 *
 * @param config The chart's configuration, supplying the label text and any configured styling.
 * @param crosshair The crosshair passed to the chart, or `null` when none was.
 * @param interactionConfig Supplies the viewport or streaming geometry used for cross-chart syncing.
 * @return The scope the chart threads through its modifier, draw pass, and overlays.
 */
@Composable
internal fun rememberWaterfallCrosshair(
    config: WaterfallChartConfig,
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
 * The crosshair anchor of every waterfall step: the top-centre of its floating bar, which sits at the
 * higher of the running totals either side of the step. This is the anchor the chart's persistent
 * markers already use.
 *
 * @param chartContext The chart's coordinate context.
 * @param cumulativeValues The running total after each step, in drawn order.
 * @return One [Offset] per step.
 */
internal fun waterfallCrosshairAnchors(
    chartContext: ChartContext,
    cumulativeValues: List<Float>,
): List<Offset> =
    verticalBarMarkerPositions(
        chartContext = chartContext,
        values = calculateWaterfallBarTopValues(cumulativeValues),
    )

/**
 * Refreshes the crosshair's anchors from the steps drawn this frame and draws the guide line,
 * highlight dot, and label. The dot takes the colour of the snapped step's direction, so a loss is
 * highlighted in the chart's negative colour. Does nothing when the chart has no crosshair.
 *
 * @param crosshair The chart's crosshair scope.
 * @param dataList The steps the crosshair reports, in drawn order.
 * @param cumulativeValues The running total after each drawn step.
 * @param chartContext The chart's coordinate context.
 * @param config The chart's configuration, supplying the positive and negative colours.
 * @param textMeasurer Measures the label.
 */
internal fun DrawScope.drawWaterfallCrosshair(
    crosshair: SeriesCrosshairScope<BarData>,
    dataList: List<BarData>,
    cumulativeValues: List<Float>,
    chartContext: ChartContext,
    config: WaterfallChartConfig,
    textMeasurer: TextMeasurer,
) {
    if (!crosshair.isActive) {
        return
    }
    drawSeriesCrosshair(
        crosshair = crosshair,
        anchors =
            waterfallCrosshairAnchors(
                chartContext = chartContext,
                cumulativeValues = cumulativeValues,
            ),
        items = dataList,
        chartContext = chartContext,
        color = waterfallCrosshairDotColor(selected = crosshair.manager?.selectedItem, config = config),
        textMeasurer = textMeasurer,
    )
}

private fun waterfallCrosshairDotColor(
    selected: BarData?,
    config: WaterfallChartConfig,
): ChartyColor =
    if (selected != null && selected.value < 0f) {
        config.negativeColor
    } else {
        config.positiveColor
    }
