package com.himanshoe.charty.common.annotation

import androidx.compose.runtime.Immutable

/**
 * Describes where an annotation attaches to a chart. Anchors are resolved to pixels at draw time
 * against the live plot bounds and value range, so an anchored annotation follows the data through
 * zoom, pan and rolling-window streaming instead of being frozen to a stale coordinate.
 */
@Immutable
sealed interface AnnotationAnchor {
    /**
     * Anchors to the horizontal centre of the category at [index], vertically centred in the plot.
     * Category positions come from the chart's own layout, so a streaming window is honoured
     * automatically. Use [Value] when the annotation must sit at a specific measured value.
     *
     * @property index Zero-based index of the data point to attach to.
     */
    data class DataPoint(
        val index: Int,
    ) : AnnotationAnchor

    /**
     * Anchors to a point in data space: [y] is mapped through the chart's value scale and [x] is a
     * continuous category position (`0f` is the first category's centre, `1f` the second's).
     *
     * @property x Continuous category position, or `null` to use the horizontal centre of the plot.
     * @property y Value on the chart's value axis.
     */
    data class Value(
        val x: Float? = null,
        val y: Float,
    ) : AnnotationAnchor

    /**
     * Anchors to raw pixels measured from the plot's top-left corner, bypassing every scale.
     *
     * @property x Horizontal offset in pixels from the plot's left edge.
     * @property y Vertical offset in pixels from the plot's top edge.
     */
    data class PixelOffset(
        val x: Float,
        val y: Float,
    ) : AnnotationAnchor
}
