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
 * A modifier extension that adds tap gesture detection for charts with rectangular bounds, such as bar charts.
 *
 * @param T The type of the data associated with each bound.
 * @param D The type of the items in the data list.
 * @param dataList The list of data items, used as a recomposition key.
 * @param bounds A list of pairs, where each pair contains the [Rect] bounds and its associated data.
 * @param onItemClick A lambda function to be invoked when an item is clicked.
 * @param onTooltipStateChange A lambda function to update the tooltip state.
 * @param createTooltipContent A function that creates a [TooltipState] from the given data and bounds.
 * @return A [Modifier] that handles tap gestures.
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
 * A modifier extension that adds tap gesture detection for point-based charts, such as line or scatter charts.
 *
 * @param T The type of the data associated with each point.
 * @param D The type of the items in the data list.
 * @param dataList The list of data items, used as a recomposition key.
 * @param pointBounds A list of pairs, where each pair contains the [Offset] position of a point and its associated data.
 * @param tapRadius The maximum radius around a point to be considered a tap.
 * @param onPointClick A lambda function to be invoked when a point is clicked.
 * @param onTooltipStateChange A lambda function to update the tooltip state.
 * @param createTooltipContent A function that creates a [TooltipState] from the given data and position.
 * @return A [Modifier] that handles tap gestures.
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
 * A helper function to create a standard [TooltipState] for items with rectangular bounds.
 *
 * @param content The text content to be displayed in the tooltip.
 * @param rect The [Rect] bounds of the clicked item.
 * @param position The preferred position of the tooltip, defined by [TooltipPosition].
 * @return A [TooltipState] instance configured for the given parameters.
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

