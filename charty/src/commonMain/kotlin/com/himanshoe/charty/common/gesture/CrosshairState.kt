package com.himanshoe.charty.common.gesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Holds the current position and value label for the crosshair overlay.
 *
 * @property x Pixel x-coordinate of the snapped data point on the canvas.
 * @property y Pixel y-coordinate of the snapped data point on the canvas.
 * @property label Pre-formatted value string displayed in the crosshair label bubble.
 */
data class CrosshairState(
    val x: Float,
    val y: Float,
    val label: String,
)

/**
 * Manages the crosshair's visible state across recompositions.
 *
 * Holds the current [CrosshairState] as observable Compose state so that the canvas
 * redraws automatically whenever the crosshair moves. Call [update] on each drag event
 * and [dismiss] when the finger lifts. Use [rememberCrosshairManager] to create an instance.
 */
class CrosshairManager {
    /**
     * The current crosshair position and label, or `null` when the crosshair is not visible.
     */
    var state: CrosshairState? by mutableStateOf(null)
        private set

    /**
     * Updates the crosshair to [newState]. Pass `null` to hide the crosshair.
     *
     * @param newState The new crosshair position and label, or `null` to dismiss.
     */
    fun update(newState: CrosshairState?) {
        state = newState
    }

    /**
     * Hides the crosshair by setting [state] to `null`.
     */
    fun dismiss() {
        state = null
    }

    /**
     * Returns `true` when the crosshair is currently visible.
     */
    fun isVisible(): Boolean = state != null
}

/**
 * Creates and remembers a [CrosshairManager] instance that survives recomposition.
 */
@Composable
fun rememberCrosshairManager(): CrosshairManager = remember { CrosshairManager() }
