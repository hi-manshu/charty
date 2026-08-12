package com.himanshoe.charty.circular.rings.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.circular.rings.data.RingData
import com.himanshoe.charty.common.config.Animation

private const val DEFAULT_RING_THICKNESS_FRACTION = 0.12f
private const val DEFAULT_RING_GAP_FRACTION = 0.04f
private const val DEFAULT_START_ANGLE_DEGREES = -90f
private const val DEFAULT_TRACK_ALPHA = 0.15f
private const val DEFAULT_LABEL_FONT_SIZE_SP = 10
private const val PERCENT_MULTIPLIER = 100

/**
 * Configuration for the appearance and behavior of an `ActivityRingsChart`.
 *
 * Ring geometry is expressed as fractions of the chart's outer radius so the rings scale with the
 * available space: each ring's stroke is `outerRadius * ringThicknessFraction` thick and adjacent
 * rings are separated by `outerRadius * ringGapFraction`. When the requested rings would not fit
 * inside the outer radius, thickness and gap are scaled down proportionally.
 *
 * @property ringThicknessFraction Stroke thickness of each ring as a fraction of the outer radius.
 *   Must be strictly between 0 and 1.
 * @property ringGapFraction Gap between adjacent rings as a fraction of the outer radius. Must be
 *   at least 0 and strictly below 1; 0 draws the rings touching.
 * @property startAngleDegrees Angle where every sweep starts: -90 is the top (12 o'clock), 0 is the
 *   right, 90 is the bottom, 180 is the left. Sweeps proceed clockwise.
 * @property roundedCaps Whether ring ends are drawn with round stroke caps; `false` uses flat caps.
 * @property trackAlpha Opacity of the faint full-circle track drawn behind each ring, between 0
 *   (invisible) and 1 (as strong as the ring itself).
 * @property showLabels Whether each ring draws the text produced by [labelFormatter] at its start
 *   angle.
 * @property labelFormatter Converts a [RingData] to the label text drawn when [showLabels] is
 *   enabled. Defaults to the ring's label plus its percentage of target, for example "Move 75%".
 * @property labelTextStyle Text style for ring labels drawn when [showLabels] is enabled.
 * @property animation Entry animation for the ring sweeps; the sweeps grow from zero to their final
 *   angle. Use [Animation.Disabled] to render the final state instantly.
 */
@Stable
data class ActivityRingsConfig(
    val ringThicknessFraction: Float = DEFAULT_RING_THICKNESS_FRACTION,
    val ringGapFraction: Float = DEFAULT_RING_GAP_FRACTION,
    val startAngleDegrees: Float = DEFAULT_START_ANGLE_DEGREES,
    val roundedCaps: Boolean = true,
    val trackAlpha: Float = DEFAULT_TRACK_ALPHA,
    val showLabels: Boolean = false,
    val labelFormatter: (RingData) -> String = { ring ->
        "${ring.label} ${((ring.value / ring.target) * PERCENT_MULTIPLIER).toInt()}%"
    },
    val labelTextStyle: TextStyle = TextStyle(fontSize = DEFAULT_LABEL_FONT_SIZE_SP.sp),
    val animation: Animation = Animation.Default,
) {
    init {
        require(ringThicknessFraction > 0f && ringThicknessFraction < 1f) {
            "ringThicknessFraction must be strictly between 0 and 1, got: $ringThicknessFraction"
        }
        require(ringGapFraction >= 0f && ringGapFraction < 1f) {
            "ringGapFraction must be in [0, 1), got: $ringGapFraction"
        }
        require(trackAlpha in 0f..1f) {
            "trackAlpha must be between 0 and 1, got: $trackAlpha"
        }
    }
}
