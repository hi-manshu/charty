package com.himanshoe.charty.bar.internal.bar.barchart

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
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
                    val clickedBar = barBounds.find { (rect, _) ->
                        rect.contains(offset)
                    }

                    clickedBar?.let { (rect, barData) ->
                        onBarClick.invoke(barData)
                        onTooltipUpdate(
                            TooltipState(
                                content = barConfig.tooltipFormatter(barData),
                                x = rect.left,
                                y = rect.top,
                                barWidth = rect.width,
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

