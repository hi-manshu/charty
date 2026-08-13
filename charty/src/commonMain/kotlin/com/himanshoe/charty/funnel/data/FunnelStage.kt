package com.himanshoe.charty.funnel.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.color.ChartyColor

/**
 * One stage of a funnel: how many of the things being tracked were still present at this step.
 *
 * Stages are given in funnel order — the widest, first stage first — and every value is a count or
 * amount, so it must be non-negative.
 *
 * @property label The stage's name, drawn on the band and read out by screen readers.
 * @property value How much reached this stage. Must be non-negative.
 * @property color Optional colour for this stage; falls back to the chart's palette position.
 */
@Immutable
data class FunnelStage(
    val label: String,
    val value: Float,
    val color: ChartyColor? = null,
) {
    init {
        require(value >= 0f) { "value must be non-negative" }
    }
}
