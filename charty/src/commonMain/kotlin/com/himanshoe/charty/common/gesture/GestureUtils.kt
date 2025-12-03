package com.himanshoe.charty.common.gesture

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Common utilities for gesture handling across different chart types
 */

/**
 * Calculate Euclidean distance between two points
 *
 * @param point1 First point
 * @param point2 Second point
 * @return The distance between the two points
 */
fun calculateDistance(point1: Offset, point2: Offset): Float {
    val dx = point1.x - point2.x
    val dy = point1.y - point2.y
    return sqrt(dx.pow(2) + dy.pow(2))
}

/**
 * Find the clicked item from a list of bounds
 *
 * @param offset The tap offset position
 * @param bounds List of pairs containing bounds and associated data
 * @return The data associated with the clicked bounds, or null if no bounds contain the offset
 */
fun <T> findClickedItem(
    offset: Offset,
    bounds: List<Pair<Rect, T>>,
): T? {
    return bounds.find { (rect, _) -> rect.contains(offset) }?.second
}

/**
 * Find the clicked item with its bounds from a list of bounds
 *
 * @param offset The tap offset position
 * @param bounds List of pairs containing bounds and associated data
 * @return The pair of bounds and data, or null if no bounds contain the offset
 */
fun <T> findClickedItemWithBounds(
    offset: Offset,
    bounds: List<Pair<Rect, T>>,
): Pair<Rect, T>? {
    return bounds.find { (rect, _) -> rect.contains(offset) }
}

/**
 * Find the nearest point within a given radius
 *
 * @param offset The tap offset position
 * @param pointBounds List of pairs containing point positions and associated data
 * @param tapRadius The maximum radius to consider for a tap
 * @return The pair of position and data if a point is within the tap radius, null otherwise
 */
fun <T> findNearestPoint(
    offset: Offset,
    pointBounds: List<Pair<Offset, T>>,
    tapRadius: Float,
): Pair<Offset, T>? {
    val nearestPoint = pointBounds.minByOrNull { pair ->
        calculateDistance(pair.first, offset)
    }

    return nearestPoint?.let { pair ->
        val distance = calculateDistance(pair.first, offset)
        if (distance <= tapRadius) {
            pair
        } else {
            null
        }
    }
}

