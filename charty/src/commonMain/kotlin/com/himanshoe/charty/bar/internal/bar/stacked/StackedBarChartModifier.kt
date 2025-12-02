package com.himanshoe.charty.bar.internal.bar.stacked

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarSegment
import com.himanshoe.charty.bar.data.BarGroup
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
                val clickedSegment = segmentBounds.find { (rect, _) ->
                    rect.contains(offset)
                }

                clickedSegment?.let { (rect, segment) ->
                    onSegmentClick.invoke(segment)
                    onTooltipStateChange(
                        TooltipState(
                            content = stackedConfig.tooltipFormatter(segment),
                            x = rect.left,
                            y = rect.top,
                            barWidth = rect.width,
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

