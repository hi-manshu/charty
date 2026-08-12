package com.himanshoe.charty.line.internal.area

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.TooltipManager
import com.himanshoe.charty.line.data.LineData

/**
 * The composable layer drawn over the area chart's canvas: the tooltip host, plus the crosshair
 * label host when a crosshair is configured. Both fill the chart box so they can position
 * themselves against the same pixel space the canvas drew into.
 *
 * @param tooltipManager Supplies the selected point and the tooltip anchor.
 * @param crosshairManager Supplies the point the crosshair snapped to, or `null` when off.
 * @param animatedCrosshairState The crosshair's resolved position, or `null` when it is not showing.
 * @param tooltip How the tooltip is presented; a canvas tooltip renders nothing here.
 * @param crosshair The crosshair to host, or `null` to skip the crosshair layer entirely.
 */
@Composable
internal fun BoxScope.AreaChartOverlays(
    tooltipManager: TooltipManager<Offset, LineData>,
    crosshairManager: CrosshairManager<LineData>?,
    animatedCrosshairState: CrosshairState?,
    tooltip: ChartTooltip<LineData>,
    crosshair: ChartCrosshair<LineData>?,
) {
    ChartTooltipHost(
        tooltip = tooltip,
        item = tooltipManager.selectedItem,
        anchor = tooltipManager.tooltipState,
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
