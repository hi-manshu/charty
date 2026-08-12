package com.himanshoe.charty.common.interaction

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.common.config.MIN_VISIBLE_WINDOW

/**
 * A preset period offered by [ChartRangeSelector] (e.g. "7D", "30D", "All").
 *
 * The selected option's [pointCount] is designed to feed a chart config's `visibleWindow`
 * (e.g. `LineChartConfig(visibleWindow = selected.pointCount)`), which already handles the
 * rolling-window clipping and slide animation. A `null` [pointCount] disables the window so the
 * chart shows its full data set.
 *
 * @property label The text shown on the option's pill button (e.g. `"7D"`, `"All"`).
 * @property pointCount How many trailing data points the option reveals, or `null` to show everything.
 */
@Immutable
data class RangeOption(
    val label: String,
    val pointCount: Int?,
) {
    init {
        require(label.isNotBlank()) { "label must not be blank" }
        require(pointCount == null || pointCount >= MIN_VISIBLE_WINDOW) {
            "pointCount must be null or >= $MIN_VISIBLE_WINDOW"
        }
    }

    /** Factory presets for the common range options. */
    companion object {
        /** The option that disables the rolling window so the chart shows every data point. */
        val All: RangeOption = RangeOption(label = "All", pointCount = null)

        /**
         * Creates an option that reveals the last [count] data points under the given [label].
         *
         * @param count How many trailing points to reveal; must be at least 2.
         * @param label The text shown on the option's pill button (e.g. `"30D"`).
         * @return A [RangeOption] whose [RangeOption.pointCount] is [count].
         */
        fun last(
            count: Int,
            label: String,
        ): RangeOption = RangeOption(label = label, pointCount = count)
    }
}
