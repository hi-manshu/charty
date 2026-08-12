package com.himanshoe.charty.circular.rings.internal

import com.himanshoe.charty.circular.rings.data.RingData
import com.himanshoe.charty.color.ChartyColor

private const val HALF_DIVIDER = 2f
private const val PERCENT_MULTIPLIER = 100

/**
 * Resolved geometry for one ring: the radius of the stroke's center line and the stroke thickness.
 */
internal data class RingLayout(
    val centerRadius: Float,
    val strokeWidth: Float,
)

/**
 * Fraction of a full revolution the ring for [value] toward [target] sweeps, coerced into `[0, 1]`
 * so overshooting values cap at one full circle. A non-positive [target] yields `0` rather than
 * letting a division by zero leak a `NaN` sweep into the draw calls.
 */
internal fun ringSweepFraction(
    value: Float,
    target: Float,
): Float =
    if (target <= 0f) {
        0f
    } else {
        (value / target).coerceIn(0f, 1f)
    }

/**
 * Fraction of the outer radius consumed by [ringCount] rings of [thicknessFraction] thickness
 * separated by [ringGapFraction] gaps.
 */
internal fun requiredRadiusFraction(
    ringCount: Int,
    thicknessFraction: Float,
    ringGapFraction: Float,
): Float = ringCount * thicknessFraction + (ringCount - 1) * ringGapFraction

/**
 * Uniform scale applied to thickness and gap so [ringCount] rings always fit inside the outer
 * radius; `1` when the requested geometry already fits.
 */
internal fun ringScale(
    ringCount: Int,
    thicknessFraction: Float,
    ringGapFraction: Float,
): Float {
    val required =
        requiredRadiusFraction(
            ringCount = ringCount,
            thicknessFraction = thicknessFraction,
            ringGapFraction = ringGapFraction,
        )
    return if (required <= 1f) {
        1f
    } else {
        1f / required
    }
}

/**
 * Computes the concentric layout for [ringCount] rings outside-in: index `0` is the outermost ring.
 * Thickness and gap are derived from [thicknessFraction] and [ringGapFraction] of [outerRadius],
 * scaled down proportionally via [ringScale] when they would not fit.
 */
internal fun ringLayouts(
    ringCount: Int,
    outerRadius: Float,
    thicknessFraction: Float,
    ringGapFraction: Float,
): List<RingLayout> {
    if (ringCount <= 0 || outerRadius <= 0f) {
        return emptyList()
    }
    val scale =
        ringScale(
            ringCount = ringCount,
            thicknessFraction = thicknessFraction,
            ringGapFraction = ringGapFraction,
        )
    val strokeWidth = outerRadius * thicknessFraction * scale
    val gap = outerRadius * ringGapFraction * scale
    return List(ringCount) { index ->
        RingLayout(
            centerRadius = outerRadius - strokeWidth / HALF_DIVIDER - index * (strokeWidth + gap),
            strokeWidth = strokeWidth,
        )
    }
}

/**
 * Resolves the effective color of the ring at [index]: the ring's own [ringColor] when set,
 * otherwise a color drawn from the [fallback] palette — a [ChartyColor.Solid] fallback colors every
 * ring the same, a [ChartyColor.Gradient] fallback assigns its colors per ring, cycling.
 */
internal fun resolveRingColor(
    ringColor: ChartyColor?,
    index: Int,
    fallback: ChartyColor,
): ChartyColor =
    ringColor ?: when (fallback) {
        is ChartyColor.Solid -> fallback
        is ChartyColor.Gradient -> ChartyColor.Solid(fallback.colors[index % fallback.colors.size])
    }

/**
 * Builds the screen-reader description for an activity-rings chart, listing each ring's label and
 * its (capped) percentage of target.
 */
internal fun buildActivityRingsDescription(rings: List<RingData>): String {
    val parts =
        rings.joinToString(separator = ", ") { ring ->
            val percent =
                (ringSweepFraction(value = ring.value, target = ring.target) * PERCENT_MULTIPLIER).toInt()
            "${ring.label} at $percent percent of target"
        }
    return "Activity rings chart with ${rings.size} rings: $parts"
}
