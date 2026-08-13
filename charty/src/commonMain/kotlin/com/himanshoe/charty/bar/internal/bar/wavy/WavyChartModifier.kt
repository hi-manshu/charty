package com.himanshoe.charty.bar.internal.bar.wavy

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * The click callback the tap handler is installed with. The wavy chart exposes no click callback of
 * its own — the tap exists purely to raise the tooltip — and a single shared instance keeps the tap
 * handler's `pointerInput` key stable across recompositions.
 */
private val TooltipOnlyTap: (BarData) -> Unit = {}

/**
 * Chains the wavy chart's pointer handling. Tap-to-tooltip and the crosshair drag are installed
 * independently — a tap never travels past touch slop and a crosshair only engages once it does —
 * so a chart configured with both answers to both.
 *
 * @param dataList The waves currently drawn, used as a recomposition key.
 * @param wavyConfig Supplies the tooltip text, placement, and styling.
 * @param barBounds The per-wave hit rects the tap handler tests, refilled each draw pass.
 * @param crosshairBounds The per-wave anchors the crosshair snaps to.
 * @param crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @param dismissOnRelease When `true`, the crosshair disappears on finger lift.
 * @param onTooltipUpdate Receives the tooltip raised by a tap, and the wave it belongs to.
 * @param tapEnabled When `false`, no tap handler is installed at all.
 * @return The chained [Modifier] to apply to the chart.
 */
internal fun buildWavyGestureModifier(
    dataList: List<BarData>,
    wavyConfig: WavyChartConfig,
    barBounds: List<Pair<Rect, BarData>>,
    crosshairBounds: List<Pair<Offset, BarData>>,
    crosshairManager: CrosshairManager<BarData>?,
    dismissOnRelease: Boolean,
    onTooltipUpdate: (TooltipState?, BarData?) -> Unit,
    tapEnabled: Boolean,
): Modifier {
    var modifier: Modifier = Modifier
    if (tapEnabled) {
        modifier =
            modifier.rectangularChartClickHandler(
                dataList = dataList,
                bounds = barBounds,
                onItemClick = TooltipOnlyTap,
                onTooltipStateChange = onTooltipUpdate,
                createTooltipContent = { barData, rect ->
                    createRectangularTooltipState(
                        content = wavyConfig.tooltipFormatter(barData),
                        rect = rect,
                        position = wavyConfig.tooltipPosition,
                    )
                },
            )
    }
    if (crosshairManager != null) {
        modifier =
            modifier.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = crosshairBounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = wavyConfig.tooltipFormatter,
                dismissOnRelease = dismissOnRelease,
            )
    }
    return modifier
}
