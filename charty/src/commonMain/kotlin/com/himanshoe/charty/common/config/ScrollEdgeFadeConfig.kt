package com.himanshoe.charty.common.config

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor

private const val DEFAULT_FADE_WIDTH = 32f
private const val DEFAULT_FADE_ALPHA = 0.9f

/**
 * Styling for the scrim drawn at the leading/trailing edges of a scrollable (zoomed/panned) chart,
 * hinting that more data lies off-screen. A fade appears on an edge only when there is data scrolled
 * past it — the left fade hides when the window is at the start, the right fade when it is at the end.
 *
 * @property width Width of each edge scrim, in pixels.
 * @property color The scrim's color at the very edge; it fades to transparent inward. Choose a color
 *   matching the chart's background so the plotted data appears to dissolve into it. A gradient runs
 *   from the edge inward before fading out.
 * @property alpha Opacity of the scrim at the edge, in `[0, 1]`.
 */
@Immutable
data class ScrollEdgeFadeConfig(
    val width: Float = DEFAULT_FADE_WIDTH,
    val color: ChartyColor = ChartyColor.Solid(Color.White),
    val alpha: Float = DEFAULT_FADE_ALPHA,
) {
    init {
        require(width >= 0f) { "Edge fade width must be non-negative" }
        require(alpha in 0f..1f) { "Edge fade alpha must be between 0 and 1" }
    }
}
