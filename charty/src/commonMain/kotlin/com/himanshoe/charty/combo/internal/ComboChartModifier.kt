package com.himanshoe.charty.combo.internal

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Add tap gesture detection for data points
 */
internal fun Modifier.comboChartClickHandler(
    dataList: List<ComboChartData>,
    comboConfig: ComboChartConfig,
    dataBounds: List<Pair<Rect, ComboChartData>>,
    onDataClick: (ComboChartData) -> Unit,
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier {
    return this.pointerInput(dataList, comboConfig, onDataClick) {
        detectTapGestures { offset ->
            val clickedData = dataBounds.find { (rect, _) ->
                rect.contains(offset)
            }

            clickedData?.let { (rect, comboData) ->
                onDataClick.invoke(comboData)
                onTooltipStateChange(
                    TooltipState(
                        content = comboConfig.tooltipFormatter(comboData),
                        x = rect.left,
                        y = rect.top,
                        barWidth = rect.width,
                        position = comboConfig.tooltipPosition,
                    ),
                )
            } ?: run {
                onTooltipStateChange(null)
            }
        }
    }
}

