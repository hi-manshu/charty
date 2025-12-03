package com.himanshoe.charty.bar.internal.bar.waterfall

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.findClickedItemWithBounds
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Create click modifier for waterfall chart
 */
internal fun createWaterfallClickModifier(
    items: List<BarData>,
    config: WaterfallChartConfig,
    barBounds: List<Pair<Rect, BarData>>,
    onBarClick: ((BarData) -> Unit)?,
    onTooltipUpdate: (TooltipState?) -> Unit,
): Modifier {
    return if (onBarClick != null) {
        Modifier.pointerInput(items, config, onBarClick) {
            detectTapGestures { offset ->
                val clickedBar = findClickedItemWithBounds(offset, barBounds)
                clickedBar?.let { (rect, barData) ->
                    onBarClick.invoke(barData)
                    onTooltipUpdate(
                        createRectangularTooltipState(
                            content = config.tooltipFormatter(barData),
                            rect = rect,
                            position = config.tooltipPosition,
                        )
                    )
                } ?: run {
                    onTooltipUpdate(null)
                }
            }
        }
    } else {
        Modifier
    }
}

