package com.himanshoe.charty.combo.internal

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.findClickedItemWithBounds
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
            val clickedData = findClickedItemWithBounds(offset, dataBounds)
            clickedData?.let { (rect, comboData) ->
                onDataClick.invoke(comboData)
                onTooltipStateChange(
                    createRectangularTooltipState(
                        content = comboConfig.tooltipFormatter(comboData),
                        rect = rect,
                        position = comboConfig.tooltipPosition,
                    ),
                )
            } ?: run {
                onTooltipStateChange(null)
            }
        }
    }
}

