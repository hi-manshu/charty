package com.himanshoe.charty.bar.internal.bar.lollipop

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.gesture.createPointTooltipState
import com.himanshoe.charty.common.gesture.pointChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState

private const val TAP_RADIUS_MULTIPLIER = 2f

/**
 * Creates a modifier with click handling for lollipop chart.
 */
internal fun createLollipopChartModifier(
    onBarClick: ((BarData) -> Unit)?,
    dataList: List<BarData>,
    config: LollipopBarChartConfig,
    lollipopBounds: List<Pair<Offset, BarData>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return if (onBarClick != null) {
        modifier.pointChartClickHandler(
            dataList = dataList,
            pointBounds = lollipopBounds,
            tapRadius = config.circleRadius * TAP_RADIUS_MULTIPLIER,
            onPointClick = onBarClick,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = { barData, position ->
                createPointTooltipState(
                    content = config.tooltipFormatter(barData),
                    position = position,
                    pointRadius = config.circleRadius,
                    tooltipPosition = config.tooltipPosition,
                    pointRadiusMultiplier = TAP_RADIUS_MULTIPLIER,
                )
            }
        )
    } else {
        modifier
    }
}

