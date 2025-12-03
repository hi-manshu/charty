package com.himanshoe.charty.common.gesture

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Common utilities for gesture handling across different chart types
 */

/**
 * Calculates the Euclidean distance between two points.
 *
 * @param point1 The first point.
 * @param point2 The second point.
 * @return The distance between the two points.
 */
fun calculateDistance(point1: Offset, point2: Offset): Float {
    val dx = point1.x - point2.x
    val dy = point1.y - point2.y
    return sqrt(dx.pow(2) + dy.pow(2))
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
): T? {
    return bounds.find { (rect, _) -> rect.contains(offset) }?.second
}

/**
 * Finds the clicked item along with its bounds from a list of bounds.
 *
 * @param T The type of the data associated with each bound.
 * @param offset The position of the tap.
 * @param bounds A list of pairs, where each pair contains the [Rect] bounds and its associated data.
 * @return A [Pair] containing the bounds and data of the clicked item, or `null` if no bounds contain the tap offset.
 */
fun <T> findClickedItemWithBounds(
    offset: Offset,
    bounds: List<Pair<Rect, T>>,
): Pair<Rect, T>? {
    return bounds.find { (rect, _) -> rect.contains(offset) }
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

