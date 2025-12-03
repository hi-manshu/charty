package com.himanshoe.charty.bar.internal.bar.mosiac

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.bar.config.MosiacBarChartConfig
import com.himanshoe.charty.bar.config.MosiacBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Creates a modifier with click handling for mosiac chart segments.
 */
@Composable
internal fun createMosiacChartModifier(
    onSegmentClick: ((MosiacBarSegment) -> Unit)?,
    groups: List<BarGroup>,
    config: MosiacBarChartConfig,
    segmentBounds: List<Pair<Rect, MosiacBarSegment>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return if (onSegmentClick != null) {
        modifier.pointerInput(groups, config, onSegmentClick) {
            detectTapGestures { offset ->
                handleMosiacSegmentClick(offset, segmentBounds, onSegmentClick, config, onTooltipUpdate)
            }
        }
    } else {
        modifier
    }
}

/**
 * Handles click events on mosiac segments.
 */
internal fun handleMosiacSegmentClick(
    offset: Offset,
    segmentBounds: List<Pair<Rect, MosiacBarSegment>>,
    onSegmentClick: (MosiacBarSegment) -> Unit,
    config: MosiacBarChartConfig,
    onTooltipUpdate: (TooltipState?) -> Unit
) {
    val clickedSegment = segmentBounds.find { (rect, _) -> rect.contains(offset) }

    clickedSegment?.let { (rect, segment) ->
        onSegmentClick.invoke(segment)
        onTooltipUpdate(
            TooltipState(
                content = config.tooltipFormatter(segment),
                x = rect.left,
                y = rect.top,
                barWidth = rect.width,
                position = config.tooltipPosition,
            )
        )
    } ?: onTooltipUpdate(null)
}

