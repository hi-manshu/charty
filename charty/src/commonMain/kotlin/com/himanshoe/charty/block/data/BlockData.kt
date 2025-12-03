package com.himanshoe.charty.block.data

import com.himanshoe.charty.color.ChartyColor

/**
 * Data model for a single block segment in [com.himanshoe.charty.block.BlockBarChart].
 *
 * @property value Relative size of this block. All block values are summed, and each
 * block's width is proportional to `value / totalValue`. Must be positive to be rendered.
 * @property color Color for this block segment. Supports solid colors or gradients via [ChartyColor].
 */
data class BlockData(
    val value: Float,
    val color: ChartyColor,
)
