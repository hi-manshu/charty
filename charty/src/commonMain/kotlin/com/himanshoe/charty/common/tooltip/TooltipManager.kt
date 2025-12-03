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
 * @param B The type of bounds (e.g., Offset for points, Rect for bars)
 * @param T The type of data associated with each tracked item (e.g., PointData, BarData, LineData)
 */
class TooltipManager<B, T> {
    /**
     * Current tooltip state, or null if no tooltip is displayed
     */
    var tooltipState: TooltipState? by mutableStateOf(null)
        private set

    /**
     * List of bounds and their associated data
     */
    val bounds = mutableListOf<Pair<B, T>>()

    /**
     * Returns bounds as a read-only List for better type compatibility
     */
    val boundsAsList: List<Pair<B, T>>
        get() = bounds

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
 * @param B The type of bounds (e.g., Offset for points, Rect for bars)
 * @param T The type of data associated with each tracked item
 * @return A remembered TooltipManager instance
 */
@Composable
fun <B, T> rememberTooltipManager(): TooltipManager<B, T> {
    return remember { TooltipManager() }
}

