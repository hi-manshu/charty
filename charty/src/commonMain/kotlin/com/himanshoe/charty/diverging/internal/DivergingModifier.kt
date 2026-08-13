package com.himanshoe.charty.diverging.internal

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.diverging.config.DivergingBarChartConfig
import com.himanshoe.charty.diverging.data.DivergingData
import com.himanshoe.charty.diverging.data.DivergingSelection

/**
 * Builds the tap — and, when enabled, drag-to-scrub — modifier for a diverging chart. Each half-bar
 * is its own hit target, so a tap reports which side of the centre axis the reader meant.
 *
 * @param modifier The modifier the handlers are chained onto.
 * @param onBarClick Called with the tapped half-bar, or `null` when taps are ignored.
 * @param dataList The rows currently drawn, used as a gesture recomposition key.
 * @param config Supplies the tooltip's text formatting and preferred placement.
 * @param barBounds The bounds recorded during the last draw, paired with what they represent.
 * @param onTooltipUpdate Receives the new tooltip state and its selection.
 * @param enableScrub Whether dragging across the chart tracks the bar under the finger.
 * @return The modifier with the gesture handlers attached.
 */
@Suppress("LongParameterList")
internal fun createDivergingChartModifier(
    modifier: Modifier,
    onBarClick: ((DivergingSelection) -> Unit)?,
    dataList: List<DivergingData>,
    config: DivergingBarChartConfig,
    barBounds: List<Pair<Rect, DivergingSelection>>,
    onTooltipUpdate: (TooltipState?, DivergingSelection?) -> Unit,
    enableScrub: Boolean,
): Modifier {
    val tooltipContentBuilder = { selection: DivergingSelection, rect: Rect ->
        TooltipState(
            content = "${selection.data.label}: ${config.valueFormatter(selection.value)}",
            x = rect.left,
            y = rect.top,
            barWidth = rect.width,
            position = config.tooltipPosition,
        )
    }
    val clickModifier =
        modifier.rectangularChartClickHandler(
            dataList = dataList,
            bounds = barBounds,
            onItemClick = onBarClick,
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
