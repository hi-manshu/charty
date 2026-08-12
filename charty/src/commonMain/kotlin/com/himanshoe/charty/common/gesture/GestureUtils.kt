package com.himanshoe.charty.common.gesture

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastMinByOrNull
import com.himanshoe.charty.common.ChartOrientation
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Calculates the Euclidean distance between two points.
 *
 * @param point1 The first point.
 * @param point2 The second point.
 * @return The distance between the two points.
 */
fun calculateDistance(
    point1: Offset,
    point2: Offset,
): Float {
    val dx = point1.x - point2.x
    val dy = point1.y - point2.y
    return sqrt(dx * dx + dy * dy)
}

/**
 * Returns the shortest distance from [offset] to the edge of this rectangle, or `0` when the
 * offset falls inside the rectangle. Used to apply a forgiving hit-slop around bar hit areas.
 */
private fun Rect.distanceTo(offset: Offset): Float {
    val dx =
        when {
            offset.x < left -> left - offset.x
            offset.x > right -> offset.x - right
            else -> 0f
        }
    val dy =
        when {
            offset.y < top -> top - offset.y
            offset.y > bottom -> offset.y - bottom
            else -> 0f
        }
    return sqrt(dx * dx + dy * dy)
}

/**
 * Finds the data associated with a clicked item from a list of bounds.
 *
 * @param T The type of the data associated with each bound.
 * @param offset The position of the tap.
 * @param bounds A list of pairs, where each pair contains the [Rect] bounds and its associated data.
 * @return The data associated with the clicked bounds, or `null` if no bounds contain the tap offset.
 */
fun <T> findClickedItem(
    offset: Offset,
    bounds: List<Pair<Rect, T>>,
): T? = bounds.fastFirstOrNull { (rect, _) -> rect.contains(offset) }?.second

/**
 * Finds the clicked item along with its bounds from a list of bounds.
 *
 * @param T The type of the data associated with each bound.
 * A tap that lands inside a bound is always matched. If none contains the tap and [hitSlop] is
 * positive, the nearest bound within [hitSlop] pixels of the tap is returned instead, so taps that
 * narrowly miss a thin bar still register.
 *
 * @param offset The position of the tap.
 * @param bounds A list of pairs, where each pair contains the [Rect] bounds and its associated data.
 * @param hitSlop Extra tolerance in pixels around each bound; `0` requires an exact hit.
 * @return A [Pair] containing the bounds and data of the clicked item, or `null` if no bounds are within range.
 */
fun <T> findClickedItemWithBounds(
    offset: Offset,
    bounds: List<Pair<Rect, T>>,
    hitSlop: Float = 0f,
): Pair<Rect, T>? {
    val exactHit = bounds.fastFirstOrNull { (rect, _) -> rect.contains(offset) }
    return exactHit ?: bounds
        .takeIf { hitSlop > 0f }
        ?.fastMinByOrNull { (rect, _) -> rect.distanceTo(offset) }
        ?.takeIf { (rect, _) -> rect.distanceTo(offset) <= hitSlop }
}

/**
 * Finds the nearest point to a given offset within a specified radius.
 *
 * @param T The type of the data associated with each point.
 * @param offset The position of the tap.
 * @param pointBounds A list of pairs, where each pair contains the [Offset] position of a point and its associated data.
 * @param tapRadius The maximum radius around a point to be considered a tap.
 * @return A [Pair] containing the position and data of the nearest point if it's within the tap radius, otherwise `null`.
 */
fun <T> findNearestPoint(
    offset: Offset,
    pointBounds: List<Pair<Offset, T>>,
    tapRadius: Float,
): Pair<Offset, T>? =
    pointBounds
        .fastMinByOrNull { (position, _) -> calculateDistance(position, offset) }
        ?.takeIf { (position, _) -> calculateDistance(position, offset) <= tapRadius }

/**
 * Finds the point whose x-coordinate is closest to [xOffset], ignoring the y-axis distance.
 *
 * This is used by the crosshair handler to snap to the nearest data point as the user drags
 * horizontally across the chart.
 *
 * @param T The type of data associated with each point.
 * @param xOffset The x position of the touch/drag event.
 * @param pointBounds A list of pairs, where each pair contains the [Offset] position of a point
 *   and its associated data.
 * @return The nearest point pair, or `null` if [pointBounds] is empty.
 */
fun <T> findNearestPointByX(
    xOffset: Float,
    pointBounds: List<Pair<Offset, T>>,
): Pair<Offset, T>? = pointBounds.fastMinByOrNull { (position, _) -> abs(position.x - xOffset) }

/**
 * Finds the point whose y-coordinate is closest to [yOffset], ignoring the x-axis distance.
 *
 * This is the horizontal-chart counterpart of [findNearestPointByX]: charts whose categories run
 * down the plot (such as [com.himanshoe.charty.bar.HorizontalBarChart]) snap the crosshair as the
 * user drags vertically across the rows.
 *
 * @param T The type of data associated with each point.
 * @param yOffset The y position of the touch/drag event.
 * @param pointBounds A list of pairs, where each pair contains the [Offset] position of a point
 *   and its associated data.
 * @return The nearest point pair, or `null` if [pointBounds] is empty.
 */
fun <T> findNearestPointByY(
    yOffset: Float,
    pointBounds: List<Pair<Offset, T>>,
): Pair<Offset, T>? = pointBounds.fastMinByOrNull { (position, _) -> abs(position.y - yOffset) }

/**
 * Finds the point nearest to [position] along the chart's category axis: by x for a
 * [ChartOrientation.VERTICAL] chart and by y for a [ChartOrientation.HORIZONTAL] one.
 *
 * @param T The type of data associated with each point.
 * @param position The current touch/drag position.
 * @param pointBounds Pairs of point positions and their associated data.
 * @param orientation The chart's orientation, which decides the axis snapping runs along.
 * @return The nearest point pair, or `null` if [pointBounds] is empty.
 */
fun <T> findNearestPointAlongCategoryAxis(
    position: Offset,
    pointBounds: List<Pair<Offset, T>>,
    orientation: ChartOrientation,
): Pair<Offset, T>? =
    when (orientation) {
        ChartOrientation.VERTICAL -> findNearestPointByX(xOffset = position.x, pointBounds = pointBounds)
        ChartOrientation.HORIZONTAL -> findNearestPointByY(yOffset = position.y, pointBounds = pointBounds)
    }

/**
 * Finds the rectangular item under a drag [position], used by the drag-to-track tooltip gesture.
 *
 * Preference order:
 * 1. A bound that geometrically contains [position] (handles stacked segments — the finger's
 *    y-coordinate disambiguates which segment in a column is selected).
 * 2. A bound whose horizontal span contains `position.x` (the column under the finger when the
 *    finger is above or below the bars).
 * 3. The bound whose horizontal centre is nearest to `position.x` (snaps across gaps).
 *
 * @param T The data associated with each bound.
 * @param position The current drag position.
 * @param bounds Pairs of [Rect] bounds and their associated data.
 * @return The matched bound and data, or `null` when [bounds] is empty.
 */
fun <T> findItemByX(
    position: Offset,
    bounds: List<Pair<Rect, T>>,
): Pair<Rect, T>? {
    val inColumn =
        bounds.fastFirstOrNull { (rect, _) -> rect.contains(position) }
            ?: bounds.fastFirstOrNull { (rect, _) -> position.x >= rect.left && position.x <= rect.right }
    return inColumn ?: bounds.fastMinByOrNull { (rect, _) -> abs(rect.center.x - position.x) }
}
