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
 * Picks the combo chart's pointer handler: the crosshair drag when a crosshair is enabled,
 * otherwise tap-to-tooltip when a click listener is set, and no handler at all when neither is.
 *
 * @param crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @param comboConfig Supplies the crosshair's dismiss behaviour and the tooltip styling.
 * @param dataList The points currently drawn, which the handlers hit-test against.
 * @param crosshairBounds The line points the crosshair snaps to.
 * @param dataBounds The drawn bar and point rects the tap handler tests.
 * @param onDataClick Invoked when a point is tapped, or `null` when taps are ignored.
 * @param onTooltipStateChange Receives the tooltip raised by a tap, and the point it belongs to.
 * @return The [Modifier] carrying the chosen handler, or an empty one.
 */
internal fun buildComboModifier(
    crosshairManager: CrosshairManager<ComboChartData>?,
    comboConfig: ComboChartConfig,
    dataList: List<ComboChartData>,
    crosshairBounds: MutableList<Pair<Offset, ComboChartData>>,
    dataBounds: MutableList<Pair<Rect, ComboChartData>>,
    onDataClick: ((ComboChartData) -> Unit)?,
    onTooltipStateChange: (TooltipState?, ComboChartData?) -> Unit,
): Modifier =
    when {
        crosshairManager != null ->
            Modifier.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = crosshairBounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = { data -> "${data.label}: ${data.lineValue}" },
                dismissOnRelease = comboConfig.crosshairConfig?.dismissOnRelease ?: true,
            )
        onDataClick != null ->
            Modifier.comboChartClickHandler(
                dataList = dataList,
                comboConfig = comboConfig,
                dataBounds = dataBounds,
                onDataClick = onDataClick,
                onTooltipStateChange = onTooltipStateChange,
            )
        else -> Modifier
    }
