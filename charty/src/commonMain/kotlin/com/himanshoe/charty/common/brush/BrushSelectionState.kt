package com.himanshoe.charty.common.brush

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Tracks the active brush-selection range drawn over a chart.
 *
 * A brush selection lets the user drag horizontally across the chart to highlight a
 * contiguous range of data points. The selected pixel range is converted to data-index
 * bounds via [toIndexRange] once the gesture completes.
 *
 * Use [rememberBrushSelectionState] to create an instance.
 */
class BrushSelectionState {
    /**
     * The pixel x-coordinate where the drag gesture started, or `null` when no selection is active.
     */
    var startX: Float? by mutableStateOf(null)
        private set

    /**
     * The pixel x-coordinate of the current drag position, or `null` when no selection is active.
     */
    var currentX: Float? by mutableStateOf(null)
        private set

    internal var chartLeft: Float = 0f
    internal var chartRight: Float = 0f
    internal var dataSize: Int = 0

    /**
     * `true` while a drag gesture is in progress and a selection rectangle is being drawn.
     */
    val isActive: Boolean get() = startX != null

    /**
     * The (min, max) pixel x-coordinates of the current selection, always ordered `start ≤ end`
     * regardless of drag direction. Returns `null` when no selection is active.
     */
    val pixelRange: Pair<Float, Float>?
        get() {
            val s = startX ?: return null
            val c = currentX ?: return null
            return if (s <= c) s to c else c to s
        }

    /**
     * Converts the current pixel selection to a pair of data indices clamped to `[0, dataSize-1]`.
     *
     * Requires the chart to have set [chartLeft], [chartRight], and [dataSize] during its draw
     * pass. Returns `null` when no selection is active or chart bounds have not yet been set.
     *
     * @return A pair of `(startIndex, endIndex)` into the chart's data list, or `null`.
     */
    fun toIndexRange(): Pair<Int, Int>? {
        val (startPx, endPx) = pixelRange ?: return null
        val totalWidth = (chartRight - chartLeft).coerceAtLeast(1f)
        val startFrac = ((startPx - chartLeft) / totalWidth).coerceIn(0f, 1f)
        val endFrac = ((endPx - chartLeft) / totalWidth).coerceIn(0f, 1f)
        val maxIndex = (dataSize - 1).coerceAtLeast(0)
        val startIdx = (startFrac * dataSize).toInt().coerceIn(0, maxIndex)
        val endIdx = (endFrac * dataSize).toInt().coerceIn(0, maxIndex)
        return startIdx to endIdx
    }

    internal fun start(x: Float) {
        startX = x
        currentX = x
    }

    internal fun update(x: Float) {
        currentX = x
    }

    internal fun clear() {
        startX = null
        currentX = null
    }
}

/**
 * Creates and remembers a [BrushSelectionState] instance that survives recomposition.
 *
 * Pass the returned state to a chart's `brushSelectionState` parameter to enable drag-to-select
 * gestures. Use the accompanying `onRangeSelect` callback to act on the committed selection.
 */
@Composable
fun rememberBrushSelectionState(): BrushSelectionState = remember { BrushSelectionState() }
