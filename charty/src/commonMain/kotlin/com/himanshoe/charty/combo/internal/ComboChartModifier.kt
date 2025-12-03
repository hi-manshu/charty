package com.himanshoe.charty.combo.internal

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
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
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier {
    return this.rectangularChartClickHandler(
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
}

