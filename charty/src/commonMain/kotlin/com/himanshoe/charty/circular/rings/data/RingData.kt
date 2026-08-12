package com.himanshoe.charty.circular.rings.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.color.ChartyColor

/**
 * A single ring of an activity-rings (solid gauge) chart.
 *
 * Each ring measures progress of [value] toward [target]; the ring's sweep is the fraction
 * `value / target`, capped at one full revolution when the value exceeds the target.
 *
 * @property label Human-readable name of the metric this ring tracks (for example "Move").
 * @property value The current progress value. Must be non-negative.
 * @property target The goal value that corresponds to a full ring. Must be positive.
 * @property color Optional color for this ring. When `null` the chart falls back to the palette
 *   passed to `ActivityRingsChart`'s `colors` parameter.
 */
@Immutable
data class RingData(
    val label: String,
    val value: Float,
    val target: Float,
    val color: ChartyColor? = null,
) {
    init {
        require(target > 0f) { "target must be positive, got: $target" }
        require(value >= 0f) { "value must be non-negative, got: $value" }
    }
}
