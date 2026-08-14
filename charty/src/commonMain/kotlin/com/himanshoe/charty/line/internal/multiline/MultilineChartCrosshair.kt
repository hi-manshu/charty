package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.drawCrosshairDot
import com.himanshoe.charty.common.gesture.drawCrosshairGuides
import com.himanshoe.charty.common.gesture.drawCrosshairValueLabel
import com.himanshoe.charty.line.data.LineGroup
import kotlin.math.abs

/**
 * Draws the crosshair overlay for a multiline chart.
 *
 * Renders a dashed vertical line at the snapped x-position, a highlight dot at each
 * series' y-coordinate, an optional horizontal line at the first series' y, and a
 * combined label bubble showing all series values.
 *
 * @param state Current crosshair position and combined label.
 * @param config Visual configuration for the crosshair.
 * @param chartContext The chart's coordinate context.
 * @param dataList The visible dataset — used to find the snapped index and per-series values.
 * @param colorList Per-series colours cycling as `colorList[seriesIndex % colorList.size]`.
 * @param textMeasurer Required for measuring and drawing the label text.
 */
internal fun DrawScope.drawMultilineChartCrosshair(
    state: CrosshairState,
    config: ChartCrosshairConfig,
    chartContext: ChartContext,
    dataList: List<LineGroup>,
    colorList: List<Color>,
    textMeasurer: TextMeasurer,
) {
    if (dataList.isEmpty() || colorList.isEmpty()) {
        return
    }
    val snappedIndex =
        dataList.indices.minByOrNull { index ->
            abs(chartContext.calculateCenteredXPosition(index, dataList.size) - state.x)
        } ?: return

    drawCrosshairGuides(state = state, config = config, chartContext = chartContext)

    dataList[snappedIndex].values.fastForEachIndexed { seriesIndex, value ->
        drawCrosshairDot(
            center = Offset(x = state.x, y = chartContext.convertValueToYPosition(value)),
            config = config,
            fill = SolidColor(colorList[seriesIndex % colorList.size]),
        )
    }

    if (config.showLabel) {
        drawCrosshairValueLabel(
            state = state,
            config = config,
            chartContext = chartContext,
            textMeasurer = textMeasurer,
        )
    }
}
