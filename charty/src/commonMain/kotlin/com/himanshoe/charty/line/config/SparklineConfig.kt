package com.himanshoe.charty.line.config

import androidx.compose.runtime.Stable
import com.himanshoe.charty.common.config.Animation

/**
 * Configuration for [Sparkline] appearance and behaviour.
 *
 * @property lineWidth Stroke width of the sparkline in pixels.
 * @property showFill Whether to draw a soft gradient fill between the line and the bottom edge.
 * @property fillAlpha Opacity of the fill in the range `[0, 1]`. Only used when [showFill] is `true`.
 * @property showLastPointDot Whether to emphasise the most recent value with a dot at the end of the line.
 * @property lastPointDotRadius Radius of the last-point dot in pixels. Only used when
 *   [showLastPointDot] is `true`. The dot's centre is kept one radius inside the draw area so the
 *   dot is never half-clipped by the edge the line ends on.
 * @property smoothCurve When `true`, connects values with a cubic-bezier curve instead of straight
 *   segments.
 * @property animation Entry animation revealing the path from left to right. [Animation.Disabled]
 *   by default so the sparkline renders instantly inline.
 */
@Stable
data class SparklineConfig(
    val lineWidth: Float = 1.5f,
    val showFill: Boolean = true,
    val fillAlpha: Float = 0.15f,
    val showLastPointDot: Boolean = true,
    val lastPointDotRadius: Float = 3f,
    val smoothCurve: Boolean = false,
    val animation: Animation = Animation.Disabled,
) {
    init {
        require(lineWidth > 0f) { "lineWidth must be greater than 0" }
        require(fillAlpha in 0f..1f) { "fillAlpha must be between 0 and 1" }
        require(lastPointDotRadius > 0f) { "lastPointDotRadius must be greater than 0" }
    }
}
