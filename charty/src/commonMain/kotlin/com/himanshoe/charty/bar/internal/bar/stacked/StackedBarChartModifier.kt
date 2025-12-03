package com.himanshoe.charty.bar.internal.bar.stacked

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.findClickedItemWithBounds
import com.himanshoe.charty.common.tooltip.TooltipState

internal fun createStackedBarChartModifier(
    dataList: List<BarGroup>,
    stackedConfig: StackedBarChartConfig,
    onSegmentClick: ((StackedBarSegment) -> Unit)?,
    segmentBounds: List<Pair<Rect, StackedBarSegment>>,
    onTooltipStateChange: (TooltipState?) -> Unit
): Modifier {
    return if (onSegmentClick != null) {
        Modifier.pointerInput(dataList, stackedConfig, onSegmentClick) {
            detectTapGestures { offset ->
                val clickedSegment = findClickedItemWithBounds(offset, segmentBounds)

                clickedSegment?.let { (rect, segment) ->
                    onSegmentClick.invoke(segment)
                    onTooltipStateChange(
                        createRectangularTooltipState(
                            content = stackedConfig.tooltipFormatter(segment),
                            rect = rect,
                            position = stackedConfig.tooltipPosition,
                        )
                    )
                } ?: run {
                    onTooltipStateChange(null)
                }
            }
        }
    } else {
        Modifier
    }
}

