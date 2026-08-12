package com.himanshoe.charty.line.internal.stackedarea

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.StackedAreaPoint

/**
 * Add tap gesture detection for stacked area chart
 */
internal fun Modifier.stackedAreaChartClickHandler(
    dataList: List<LineGroup>,
    lineConfig: LineChartConfig,
    areaSegmentBounds: List<Triple<Rect, Path, StackedAreaPoint>>,
    onAreaClick: (StackedAreaPoint) -> Unit,
    onTooltipStateChange: (TooltipState?, StackedAreaPoint?) -> Unit,
): Modifier {
    val bounds = areaSegmentBounds.fastMap { (rect, _, point) -> rect to point }

    return this.rectangularChartClickHandler(
        dataList = dataList,
        bounds = bounds,
        onItemClick = onAreaClick,
        onTooltipStateChange = onTooltipStateChange,
        createTooltipContent = { areaPoint, rect ->
            TooltipState(
                content = "${areaPoint.lineGroup.label}: ${areaPoint.value}",
                x = rect.left + rect.width / StackedAreaChartConstants.BEZIER_CONTROL_POINT_2_MULTIPLIER,
                y = rect.top,
                barWidth = rect.width,
                position = lineConfig.tooltipPosition,
            )
        },
    )
}

/**
 * Picks the stacked area chart's pointer handler: the crosshair drag when a crosshair is enabled,
 * otherwise tap-to-tooltip when a click listener is set, and no handler at all when neither is.
 *
 * @param crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @param lineConfig Supplies the crosshair's dismiss behaviour and the tooltip styling.
 * @param dataList The groups currently drawn, which the handlers hit-test against.
 * @param crosshairBounds The topmost stack points the crosshair snaps to.
 * @param areaSegmentBounds The drawn segments the tap handler tests.
 * @param onAreaClick Invoked when an area is tapped, or `null` when taps are ignored.
 * @param onTooltipStateChange Receives the tooltip raised by a tap, and the point it belongs to.
 * @return The [Modifier] carrying the chosen handler, or an empty one.
 */
internal fun buildStackedAreaModifier(
    crosshairManager: CrosshairManager<LineGroup>?,
    lineConfig: LineChartConfig,
    dataList: List<LineGroup>,
    crosshairBounds: MutableList<Pair<Offset, LineGroup>>,
    areaSegmentBounds: MutableList<Triple<Rect, Path, StackedAreaPoint>>,
    onAreaClick: ((StackedAreaPoint) -> Unit)?,
    onTooltipStateChange: (TooltipState?, StackedAreaPoint?) -> Unit,
): Modifier =
    when {
        crosshairManager != null ->
            Modifier.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = crosshairBounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = { group -> "${group.label}: ${group.values.sum()}" },
                dismissOnRelease = lineConfig.crosshairConfig?.dismissOnRelease ?: true,
            )
        onAreaClick != null ->
            Modifier.stackedAreaChartClickHandler(
                dataList = dataList,
                lineConfig = lineConfig,
                areaSegmentBounds = areaSegmentBounds,
                onAreaClick = onAreaClick,
                onTooltipStateChange = onTooltipStateChange,
            )
        else -> Modifier
    }
