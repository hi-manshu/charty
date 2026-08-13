package com.himanshoe.charty.funnel.internal

/**
 * The slice of the flow axis one stage occupies: where its band begins and ends along the direction
 * the funnel runs in (downwards for a vertical funnel, rightwards for a horizontal one).
 *
 * @property start The band's leading edge in pixels.
 * @property end The band's trailing edge in pixels.
 */
internal data class FunnelBand(
    val start: Float,
    val end: Float,
) {
    /** The band's length along the flow axis. */
    val extent: Float get() = end - start
}
