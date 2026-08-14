package com.himanshoe.charty.common

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * The Composable layer every chart draws over its canvas: the tooltip host, then the crosshair label
 * host. Both fill the chart box so they position themselves against the same pixel space the canvas
 * drew into, and each renders nothing when it is not configured — a chart can host both at once.
 *
 * Seven charts had written this out, and the seven bodies differed only in their type arguments and
 * in where the tooltip's item came from. Stacking order matters here: the crosshair label is drawn
 * last so it sits above the tooltip when a reader drags the crosshair under one. That is the kind of
 * detail that stays consistent only while there is one copy of it.
 *
 * @param tooltip How the tooltip is presented, or `null` for a chart with no tooltip layer at all.
 *   A canvas tooltip renders nothing here.
 * @param tooltipItem The selected item the tooltip describes, or `null` when nothing is selected.
 * @param tooltipAnchor Where on the canvas the tooltip points, or `null` when nothing is selected.
 * @param crosshair The crosshair to host, or `null` to skip the crosshair layer entirely.
 * @param crosshairItem The point the crosshair snapped to, or `null` when it is not showing.
 * @param crosshairState The crosshair's resolved position, or `null` when it is not showing.
 */
@Composable
internal fun <TooltipItem, CrosshairItem> BoxScope.ChartOverlays(
    tooltip: ChartTooltip<TooltipItem>?,
    tooltipItem: TooltipItem?,
    tooltipAnchor: TooltipState?,
    crosshair: ChartCrosshair<CrosshairItem>?,
    crosshairItem: CrosshairItem?,
    crosshairState: CrosshairState?,
) {
    if (tooltip != null) {
        ChartTooltipHost(
            tooltip = tooltip,
            item = tooltipItem,
            anchor = tooltipAnchor,
            modifier = Modifier.matchParentSize(),
        )
    }
    if (crosshair != null) {
        ChartCrosshairHost(
            crosshair = crosshair,
            item = crosshairItem,
            state = crosshairState,
            modifier = Modifier.matchParentSize(),
        )
    }
}
