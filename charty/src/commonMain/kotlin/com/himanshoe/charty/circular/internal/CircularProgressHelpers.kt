package com.himanshoe.charty.circular.internal

/**
 * Calculate the stroke width for each ring based on available space
 */
internal fun calculateStrokeWidth(
    radius: Float,
    centerHoleRatio: Float,
    gapBetweenRings: Float,
    ringCount: Int,
): Float {
    if (ringCount == 0) return 0f

    val centerHoleSize = radius * centerHoleRatio
    val availableRadius = radius - centerHoleSize
    val totalGapSpace = gapBetweenRings * (ringCount - 1)
    val availableForStrokes = availableRadius - totalGapSpace
    return (availableForStrokes / ringCount).coerceAtLeast(1f)
}

/**
 * Calculate the radius for a specific ring based on its index
 */
internal fun calculateRingRadius(
    index: Int,
    radius: Float,
    gapBetweenRings: Float,
    strokeWidth: Float,
): Float {
    val accumulatedWidth = index * (strokeWidth + gapBetweenRings)
    return radius - accumulatedWidth - (strokeWidth / CircularProgressConstants.TWO)
}

