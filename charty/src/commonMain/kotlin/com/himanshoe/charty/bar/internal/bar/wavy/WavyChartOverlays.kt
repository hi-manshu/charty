package com.himanshoe.charty.bar.internal.bar.wavy

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState

/**
 * The composable layer drawn over the wavy chart's canvas: the crosshair label host, filling the
 * chart box so it can position itself against the same pixel space the canvas drew into. Renders
 * nothing when no crosshair is configured.
 *
 * @param crosshairManager Supplies the bar the crosshair snapped to, or `null` when off.
 * @param animatedCrosshairState The crosshair's resolved position, or `null` when it is not showing.
 * @param crosshair The crosshair to host, or `null` to skip the crosshair layer entirely.
 */
@Composable
internal fun BoxScope.WavyChartOverlays(
    crosshairManager: CrosshairManager<BarData>?,
    animatedCrosshairState: CrosshairState?,
    crosshair: ChartCrosshair<BarData>?,
) {
    if (crosshair != null) {
        ChartCrosshairHost(
            crosshair = crosshair,
            item = crosshairManager?.selectedItem,
            state = animatedCrosshairState,
            modifier = Modifier.matchParentSize(),
        )
    }
}
