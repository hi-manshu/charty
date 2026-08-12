package com.himanshoe.charty.calendar.internal

/**
 * Decides which axis labels can be drawn without overlapping their neighbours.
 *
 * The labels are walked in the order given — which must be the order in which they appear along
 * the axis — while tracking the trailing edge of the most recently kept label. A label is kept
 * only when its leading edge starts at or after that trailing edge plus [minimumGap]; otherwise
 * it is dropped entirely, which keeps every drawn label fully legible instead of truncating or
 * shrinking text. The first label is always kept.
 *
 * The function is deliberately free of any Compose or geometry types so it can be unit-tested and
 * reused for both horizontal (month) and vertical (day-of-week) labels.
 *
 * @param positions Leading edge of each label along the axis, in pixels, in draw order.
 * @param sizes Extent of each label along the same axis, in pixels; must match [positions] in size.
 * @param minimumGap Minimum empty space, in pixels, required between two consecutive drawn labels.
 *   Negative values are treated as zero.
 * @return One flag per input label: `true` when the label should be drawn, `false` when it should
 *   be skipped.
 */
internal fun selectNonOverlappingLabels(
    positions: List<Float>,
    sizes: List<Float>,
    minimumGap: Float,
): List<Boolean> {
    require(positions.size == sizes.size) {
        "positions and sizes must have the same size, got ${positions.size} and ${sizes.size}"
    }
    if (positions.isEmpty()) {
        return emptyList()
    }
    val gap = minimumGap.coerceAtLeast(0f)
    val keep = MutableList(positions.size) { false }
    var lastEnd = Float.NEGATIVE_INFINITY
    positions.forEachIndexed { index, position ->
        if (index == 0 || position >= lastEnd + gap) {
            keep[index] = true
            lastEnd = position + sizes[index]
        }
    }
    return keep
}
