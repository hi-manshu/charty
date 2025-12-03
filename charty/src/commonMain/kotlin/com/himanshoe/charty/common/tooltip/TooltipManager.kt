package com.himanshoe.charty.common.tooltip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Manager for tooltip state and bounds tracking.
 * Provides a centralized way to manage tooltip display and data point bounds.
 *
 * @param T The type of data associated with each tracked item (e.g., PointData, BarData, LineData)
 */
class TooltipManager<T> {
    /**
     * Current tooltip state, or null if no tooltip is displayed
     */
    var tooltipState: TooltipState? by mutableStateOf(null)
        private set

    /**
     * List of bounds and their associated data
     * The first element can be Offset (for points) or Rect (for bars)
     */
    val bounds = mutableListOf<Pair<Any, T>>()

    /**
     * Update the tooltip state
     */
    fun updateTooltip(state: TooltipState?) {
        tooltipState = state
    }

    /**
     * Clear all tracked bounds
     * Should be called at the start of each draw cycle
     */
    fun clearBounds() {
        bounds.clear()
    }

    /**
     * Dismiss the currently displayed tooltip
     */
    fun dismiss() {
        tooltipState = null
    }

    /**
     * Check if a tooltip is currently displayed
     */
    fun isVisible(): Boolean = tooltipState != null
}

/**
 * Creates and remembers a TooltipManager instance
 *
 * @param T The type of data associated with each tracked item
 * @return A remembered TooltipManager instance
 */
@Composable
fun <T> rememberTooltipManager(): TooltipManager<T> {
    return remember { TooltipManager() }
}

