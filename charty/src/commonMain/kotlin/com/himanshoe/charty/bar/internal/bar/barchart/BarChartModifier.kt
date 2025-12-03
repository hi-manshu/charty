package com.himanshoe.charty.bar.internal.bar.barchart

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.findClickedItemWithBounds
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Helper function to create the chart modifier with tap gestures for bar clicks
 */
@Composable
internal fun createBarChartModifier(
    onBarClick: ((BarData) -> Unit)?,
    dataList: List<BarData>,
    barConfig: BarChartConfig,
    barBounds: List<Pair<Rect, BarData>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return modifier.then(
        if (onBarClick != null) {
            Modifier.pointerInput(dataList, barConfig, onBarClick) {
                detectTapGestures { offset ->
                    val clickedBar = findClickedItemWithBounds(offset = offset, bounds = barBounds)
                    clickedBar?.let { (rect, barData) ->
                        onBarClick.invoke(barData)
                        onTooltipUpdate(
                            createRectangularTooltipState(
                                content = barConfig.tooltipFormatter(barData),
                                rect = rect,
                                position = barConfig.tooltipPosition,
                            ),
                        )
                    } ?: run {
                        onTooltipUpdate(null)
                    }
                }
            }
        } else {
            Modifier
        },
    )
}

