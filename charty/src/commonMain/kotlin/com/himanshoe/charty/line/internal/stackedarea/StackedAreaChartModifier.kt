package com.himanshoe.charty.line.internal.stackedarea

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.common.gesture.findClickedItemWithBounds
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
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier {
    return this.pointerInput(dataList, lineConfig, onAreaClick) {
        detectTapGestures { offset ->
            val bounds = areaSegmentBounds.fastMap { (rect, _, point) -> rect to point }
            val clickedSegment = findClickedItemWithBounds(offset, bounds)

            clickedSegment?.let { (bounds, areaPoint) ->
                onAreaClick.invoke(areaPoint)
                onTooltipStateChange(
                    TooltipState(
                        content = "${areaPoint.lineGroup.label}: ${areaPoint.value}",
                        x = bounds.left + bounds.width / StackedAreaChartConstants.BEZIER_CONTROL_POINT_2_MULTIPLIER,
                        y = bounds.top,
                        barWidth = bounds.width,
                        position = lineConfig.tooltipPosition,
                    ),
                )
            } ?: run {
                onTooltipStateChange(null)
            }
        }
    }
}



