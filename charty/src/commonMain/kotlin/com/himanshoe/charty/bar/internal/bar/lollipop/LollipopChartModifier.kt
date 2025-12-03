package com.himanshoe.charty.bar.internal.bar.lollipop

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.tooltip.TooltipState

private const val TAP_RADIUS_MULTIPLIER = 2f

/**
 * Creates a modifier with click handling for lollipop chart.
 */
@Composable
internal fun createLollipopChartModifier(
    onBarClick: ((BarData) -> Unit)?,
    dataList: List<BarData>,
    config: LollipopBarChartConfig,
    lollipopBounds: List<Pair<Offset, BarData>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return if (onBarClick != null) {
        modifier.pointerInput(dataList, config, onBarClick) {
            detectTapGestures { offset ->
                handleLollipopClick(offset, lollipopBounds, onBarClick, config, onTooltipUpdate)
            }
        }
    } else {
        modifier
    }
}

/**
 * Handles click events on lollipops.
 */
internal fun handleLollipopClick(
    offset: Offset,
    lollipopBounds: List<Pair<Offset, BarData>>,
    onBarClick: (BarData) -> Unit,
    config: LollipopBarChartConfig,
    onTooltipUpdate: (TooltipState?) -> Unit,
) {
    val tapRadius = config.circleRadius * TAP_RADIUS_MULTIPLIER
    val clickedLollipop = lollipopBounds.minByOrNull { (position, _) ->
        calculateDistance(position, offset)
    }

    clickedLollipop?.let { (position, barData) ->
        val distance = calculateDistance(position, offset)

        if (distance <= tapRadius) {
            onBarClick.invoke(barData)
            onTooltipUpdate(
                TooltipState(
                    content = config.tooltipFormatter(barData),
                    x = position.x - config.circleRadius,
                    y = position.y,
                    barWidth = config.circleRadius * TAP_RADIUS_MULTIPLIER,
                    position = config.tooltipPosition,
                ),
            )
        } else {
            onTooltipUpdate(null)
        }
    } ?: onTooltipUpdate(null)
}

