package com.himanshoe.charty.common.gesture

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Common modifier extensions for handling chart interactions with gestures
 */

/**
 * Add tap gesture detection for rectangular bounds (bars, segments, etc.)
 *
 * @param dataList The list of data items (for recomposition key)
 * @param bounds List of pairs containing bounds and associated data
 * @param onItemClick Callback when an item is clicked
 * @param onTooltipStateChange Callback to update tooltip state
 * @param createTooltipContent Function to create tooltip content from data and bounds
 */
fun <T, D> Modifier.rectangularChartClickHandler(
    dataList: List<D>,
    bounds: List<Pair<Rect, T>>,
    onItemClick: ((T) -> Unit)?,
    onTooltipStateChange: (TooltipState?) -> Unit,
    createTooltipContent: (T, Rect) -> TooltipState,
): Modifier {
    return if (onItemClick != null) {
        this.pointerInput(dataList, onItemClick) {
            detectTapGestures { offset ->
                val clickedItem = findClickedItemWithBounds(offset, bounds)

                clickedItem?.let { (rect, data) ->
                    onItemClick.invoke(data)
                    onTooltipStateChange(createTooltipContent(data, rect))
                } ?: run {
                    onTooltipStateChange(null)
                }
            }
        }
    } else {
        this
    }
}

/**
 * Add tap gesture detection for point-based charts (line, scatter, etc.)
 *
 * @param dataList The list of data items (for recomposition key)
 * @param pointBounds List of pairs containing point positions and associated data
 * @param tapRadius The maximum radius to consider for a tap
 * @param onPointClick Callback when a point is clicked
 * @param onTooltipStateChange Callback to update tooltip state
 * @param createTooltipContent Function to create tooltip content from data and position
 */
fun <T, D> Modifier.pointChartClickHandler(
    dataList: List<D>,
    pointBounds: List<Pair<Offset, T>>,
    tapRadius: Float,
    onPointClick: (T) -> Unit,
    onTooltipStateChange: (TooltipState?) -> Unit,
    createTooltipContent: (T, Offset) -> TooltipState,
): Modifier {
    return this.pointerInput(dataList, onPointClick) {
        detectTapGestures { offset ->
            val nearestPoint = findNearestPoint(offset, pointBounds, tapRadius)

            nearestPoint?.let { (position, data) ->
                onPointClick.invoke(data)
                onTooltipStateChange(createTooltipContent(data, position))
            } ?: run {
                onTooltipStateChange(null)
            }
        }
    }
}

/**
 * Helper to create a standard tooltip state for rectangular bounds
 *
 * @param content The tooltip content text
 * @param rect The bounds of the clicked item
 * @param position The preferred tooltip position
 */
fun createRectangularTooltipState(
    content: String,
    rect: Rect,
    position: TooltipPosition = TooltipPosition.AUTO,
): TooltipState {
    return TooltipState(
        content = content,
        x = rect.left,
        y = rect.top,
        barWidth = rect.width,
        position = position,
    )
}

/**
 * Helper to create a standard tooltip state for point positions
 *
 * @param content The tooltip content text
 * @param position The point position
 * @param pointRadius The radius of the point
 * @param tooltipPosition The preferred tooltip position
 * @param pointRadiusMultiplier Multiplier for the point radius in tooltip (default 2.0)
 */
fun createPointTooltipState(
    content: String,
    position: Offset,
    pointRadius: Float,
    tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    pointRadiusMultiplier: Float = 2f,
): TooltipState {
    return TooltipState(
        content = content,
        x = position.x - pointRadius,
        y = position.y,
        barWidth = pointRadius * pointRadiusMultiplier,
        position = tooltipPosition,
    )
}

