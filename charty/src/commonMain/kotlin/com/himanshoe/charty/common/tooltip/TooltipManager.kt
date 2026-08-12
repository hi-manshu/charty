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
     * The data item the tooltip currently points to, or null if no tooltip is displayed.
     *
     * This is what a custom tooltip composable renders. It is set alongside [tooltipState] when a
     * data point is selected and cleared when the tooltip is dismissed.
     */
    var selectedItem: T? by mutableStateOf(null)
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
     * Update the tooltip state, optionally recording the data [item] it points to.
     *
     * The [item] is what a custom tooltip composable renders. Callers that build a [TooltipState]
     * from a data point should pass that point here so custom tooltips can access it. When [item]
     * is omitted (the default), [selectedItem] is cleared.
     *
     * @param state The new tooltip state, or `null` to hide the tooltip.
     * @param item The data point the tooltip points to, or `null` to clear it.
     */
    fun updateTooltip(
        state: TooltipState?,
        item: T? = null,
    ) {
        tooltipState = state
        selectedItem = item
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
        selectedItem = null
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
fun <B, T> rememberTooltipManager(): TooltipManager<B, T> = remember { TooltipManager() }

/**
 * A [TooltipManager] that forgets its tooltip whenever [dataKey] changes.
 *
 * A tooltip is anchored to a pixel position, so once the series behind it changes — most visibly on
 * a streaming chart, where the tapped point slides away on every append — that anchor no longer
 * points at the data it describes. Rebuilding the manager clears the stale bubble instead of leaving
 * it pinned over unrelated points.
 *
 * @param dataKey The value whose change invalidates the current tooltip, normally the drawn series.
 */
@Composable
fun <B, T> rememberTooltipManager(dataKey: Any?): TooltipManager<B, T> = remember(dataKey) { TooltipManager() }
