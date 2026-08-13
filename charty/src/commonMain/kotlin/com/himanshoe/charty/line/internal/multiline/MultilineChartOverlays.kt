package com.himanshoe.charty.line.internal.multiline

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.data.MultilinePoint

/**
 * The composable layer drawn over the multiline chart's canvas: the Compose-overlay tooltip and the
 * crosshair label host, both filling the chart box so they position themselves against the same
 * pixel space the canvas drew into. Each layer renders nothing when it is not configured, and they
 * are independent — a chart can host both at once.
 *
 * @param crosshairManager Supplies the point the crosshair snapped to, or `null` when off.
 * @param animatedCrosshairState The crosshair's resolved position, or `null` when it is not showing.
 * @param crosshair The crosshair to host, or `null` to skip the crosshair layer entirely.
 * @param tooltip The tooltip strategy; only [ChartTooltip.compose] renders here.
 * @param tooltipItem The tapped point the tooltip describes, or `null` when nothing is selected.
 * @param tooltipAnchor The tooltip's anchor on the canvas, or `null` when nothing is selected.
 */
@Composable
internal fun BoxScope.MultilineChartOverlays(
    crosshairManager: CrosshairManager<MultilinePoint>?,
    animatedCrosshairState: CrosshairState?,
    crosshair: ChartCrosshair<MultilinePoint>?,
    tooltip: ChartTooltip<MultilinePoint>,
    tooltipItem: MultilinePoint?,
    tooltipAnchor: TooltipState?,
) {
    ChartTooltipHost(
        tooltip = tooltip,
        item = tooltipItem,
        anchor = tooltipAnchor,
        modifier = Modifier.matchParentSize(),
    )

    if (crosshair != null) {
        ChartCrosshairHost(
            crosshair = crosshair,
            item = crosshairManager?.selectedItem,
            state = animatedCrosshairState,
            modifier = Modifier.matchParentSize(),
        )
    }
}
