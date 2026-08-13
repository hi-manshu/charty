package com.himanshoe.charty.bar.internal.bar.bubblebar

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * The stand-in click callback used when the chart is tapped for a tooltip but the caller passed no
 * click callback of its own. It is a single shared instance so the tap handler's `pointerInput` key
 * stays stable across recompositions.
 */
private val TooltipOnlyTap: (BarData) -> Unit = {}

/**
 * Chains the bubble bar chart's pointer handling. The tap handler is installed whenever [tapEnabled]
 * is `true` — that is, when the caller wants a click callback, a tooltip, or both — and it raises the
 * tooltip and invokes [onBarClick] from the same tap, so neither displaces the other. When
 * [enableScrub] is `true` a drag-to-track tooltip gesture is layered on top of it.
 *
 * @param onBarClick Invoked when a column is tapped, or `null` when the caller ignores clicks.
 * @param dataList The columns currently drawn, used as a recomposition key.
 * @param bubbleConfig Supplies the tooltip text, placement, and styling.
 * @param barBounds The per-column rects the tap handler tests, refilled each draw pass.
 * @param onTooltipUpdate Receives the tooltip raised by a tap, and the column it belongs to.
 * @param modifier The modifier the handlers are chained onto.
 * @param enableScrub When `true`, adds the drag-to-track tooltip gesture.
 * @param tapEnabled When `false`, no tap handler is installed at all.
 * @return The chained [Modifier] to apply to the chart.
 */
internal fun createBubbleChartModifier(
    onBarClick: ((BarData) -> Unit)?,
    dataList: List<BarData>,
    bubbleConfig: BubbleBarChartConfig,
    barBounds: List<Pair<Rect, BarData>>,
    onTooltipUpdate: (TooltipState?, BarData?) -> Unit,
    modifier: Modifier = Modifier,
    enableScrub: Boolean = false,
    tapEnabled: Boolean = onBarClick != null,
): Modifier {
    val tooltipContentBuilder = { barData: BarData, rect: Rect ->
        TooltipState(
            content = bubbleConfig.tooltipFormatter(barData),
            x = rect.left,
            y = rect.top,
            barWidth = rect.width,
            position = bubbleConfig.tooltipPosition,
        )
    }
    val clickModifier =
        modifier.rectangularChartClickHandler(
            dataList = dataList,
            bounds = barBounds,
            onItemClick =
                if (tapEnabled) {
                    onBarClick ?: TooltipOnlyTap
                } else {
                    null
                },
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = tooltipContentBuilder,
        )
    return if (enableScrub) {
        clickModifier.rectangularChartScrubHandler(
            dataList = dataList,
            bounds = barBounds,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = tooltipContentBuilder,
        )
    } else {
        clickModifier
    }
}
