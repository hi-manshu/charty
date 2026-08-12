package com.himanshoe.charty.common

/**
 * A holder that receives the plot's horizontal pixel bounds on every draw pass, so features which
 * need to convert between pixels and normalised positions can read them without owning the geometry
 * themselves.
 *
 * Both [com.himanshoe.charty.common.viewport.ViewPortState] and
 * [com.himanshoe.charty.common.streaming.StreamingState] implement it, which is what lets the synced
 * crosshair work in either mode: a chart configured for zoom and pan supplies its viewport, and a
 * streaming chart — which by definition has no viewport — supplies its streaming state instead.
 */
internal interface PlotBoundsSource {
    /** Canvas x-coordinate of the plot's left edge. */
    val plotLeft: Float

    /** Width of the plot in pixels. */
    val plotWidth: Float
}
