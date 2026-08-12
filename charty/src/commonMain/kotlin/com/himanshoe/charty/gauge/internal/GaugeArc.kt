package com.himanshoe.charty.gauge.internal

/**
 * An arc segment of a gauge dial in the Compose drawing convention: degrees, `0` at 3 o'clock,
 * positive clockwise.
 *
 * @property startAngleDegrees The angle at which the arc starts.
 * @property sweepAngleDegrees The angular extent of the arc; never negative.
 */
internal data class GaugeArc(
    val startAngleDegrees: Float,
    val sweepAngleDegrees: Float,
)
