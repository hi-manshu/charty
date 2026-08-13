package com.himanshoe.charty.diverging.internal

import com.himanshoe.charty.diverging.data.DivergingSide

/**
 * The magnitude each half of a diverging chart is scaled against.
 *
 * @property left The magnitude that fills the left half of the plot.
 * @property right The magnitude that fills the right half of the plot.
 */
internal data class DivergingScales(
    val left: Float,
    val right: Float,
) {
    /** The larger of the two scales, which the symmetric value axis is built from. */
    val axisScale: Float get() = maxOf(left, right)

    /** The scale that applies to [side]. */
    fun forSide(side: DivergingSide): Float =
        when (side) {
            DivergingSide.LEFT -> left
            DivergingSide.RIGHT -> right
        }
}
