package com.himanshoe.charty.gauge.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.color.ChartyColor

/**
 * A colored zone painted on the dial of an angular gauge, spanning a sub-range of the gauge's value
 * range. Plot bands are typically used to mark qualitative regions such as "safe", "warning", and
 * "critical" on a speedometer-style dial.
 *
 * @property fromValue The value at which the band starts. Must be strictly less than [toValue].
 * @property toValue The value at which the band ends. Must be strictly greater than [fromValue].
 * @property color The color used to paint the band arc, either solid or gradient.
 */
@Immutable
data class GaugeBand(
    val fromValue: Float,
    val toValue: Float,
    val color: ChartyColor,
) {
    init {
        require(toValue > fromValue) {
            "toValue ($toValue) must be greater than fromValue ($fromValue)"
        }
    }
}
