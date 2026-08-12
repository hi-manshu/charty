package com.himanshoe.charty.bar.internal.bar.horizontal

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
import com.himanshoe.charty.common.tooltip.TooltipManager

/**
 * Adds drag-to-track tooltips to [base] when the caller opted in and nothing else is competing for
 * the drag, so sliding a finger along the bars keeps a tooltip pinned to the one under it. Returns
 * [base] untouched otherwise.
 *
 * @param base The modifier the handler is chained onto.
 * @param interactionConfig Decides whether drag-to-track tooltips are active.
 * @param dataList The bars currently drawn, which the handler hit-tests against.
 * @param barConfig Supplies the tooltip's text formatter and placement.
 * @param tooltipManager Owns the bar bounds the handler tests and the tooltip it raises.
 * @return [base] with the scrub handler chained on, or [base] itself.
 */
internal fun horizontalBarScrubModifier(
    base: Modifier,
    interactionConfig: ChartInteractionConfig,
    dataList: List<BarData>,
    barConfig: BarChartConfig,
    tooltipManager: TooltipManager<Rect, BarData>,
): Modifier =
    if (interactionConfig.dragTooltipActive) {
        base.rectangularChartScrubHandler(
            dataList = dataList,
            bounds = tooltipManager.bounds,
            onTooltipStateChange = tooltipManager::updateTooltip,
            createTooltipContent = { barData, rect ->
                createRectangularTooltipState(
                    content = barConfig.tooltipFormatter(barData),
                    rect = rect,
                    position = barConfig.tooltipPosition,
                )
            },
        )
    } else {
        base
    }
