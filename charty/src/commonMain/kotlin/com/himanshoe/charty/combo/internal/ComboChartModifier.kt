package com.himanshoe.charty.combo.internal

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.util.toChartLabel

/**
 * The stand-in click callback used when the chart is tapped for a tooltip but the caller passed no
 * click callback of its own. It is a single shared instance so the tap handler's `pointerInput` key
 * stays stable across recompositions.
 */
private val TooltipOnlyTap: (ComboChartData) -> Unit = {}

/**
 * Add tap gesture detection for data points
 */
internal fun Modifier.comboChartClickHandler(
    dataList: List<ComboChartData>,
    comboConfig: ComboChartConfig,
    dataBounds: List<Pair<Rect, ComboChartData>>,
    onDataClick: (ComboChartData) -> Unit,
    onTooltipStateChange: (TooltipState?, ComboChartData?) -> Unit,
): Modifier =
    this.rectangularChartClickHandler(
        dataList = dataList,
        bounds = dataBounds,
        onItemClick = onDataClick,
        onTooltipStateChange = onTooltipStateChange,
        createTooltipContent = { comboData, rect ->
            createRectangularTooltipState(
                content = comboConfig.tooltipFormatter(comboData),
                rect = rect,
                position = comboConfig.tooltipPosition,
            )
        },
    )

/**
 * Chains the combo chart's pointer handlers. Tap-to-tooltip and the crosshair drag are installed
 * independently — a tap never travels past touch slop and a crosshair only engages once it does —
 * so a chart configured with both answers to both, and an empty [Modifier] is returned when neither
 * is configured.
 *
 * @param crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @param comboConfig Supplies the crosshair's dismiss behaviour and the tooltip styling.
 * @param dataList The points currently drawn, which the handlers hit-test against.
 * @param crosshairBounds The line points the crosshair snaps to.
 * @param dataBounds The drawn bar and point rects the tap handler tests.
 * @param onDataClick Invoked when a point is tapped, or `null` when the caller ignores clicks.
 * @param onTooltipStateChange Receives the tooltip raised by a tap, and the point it belongs to.
 * @param tapEnabled When `false`, no tap handler is installed at all.
 * @return The [Modifier] carrying the configured handlers, or an empty one.
 */
internal fun buildComboModifier(
    crosshairManager: CrosshairManager<ComboChartData>?,
    comboConfig: ComboChartConfig,
    dataList: List<ComboChartData>,
    crosshairBounds: MutableList<Pair<Offset, ComboChartData>>,
    dataBounds: MutableList<Pair<Rect, ComboChartData>>,
    onDataClick: ((ComboChartData) -> Unit)?,
    onTooltipStateChange: (TooltipState?, ComboChartData?) -> Unit,
    tapEnabled: Boolean = onDataClick != null,
): Modifier {
    var mod: Modifier = Modifier
    if (tapEnabled) {
        mod =
            mod.comboChartClickHandler(
                dataList = dataList,
                comboConfig = comboConfig,
                dataBounds = dataBounds,
                onDataClick = onDataClick ?: TooltipOnlyTap,
                onTooltipStateChange = onTooltipStateChange,
            )
    }
    if (crosshairManager != null) {
        mod =
            mod.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = crosshairBounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = { data -> "${data.label}: ${data.lineValue.toChartLabel()}" },
                dismissOnRelease = comboConfig.crosshairConfig?.dismissOnRelease ?: true,
            )
    }
    return mod
}
