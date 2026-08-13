package com.himanshoe.charty.diverging.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.color.ChartyColor

/**
 * One category row of a diverging bar chart: a label plus the two opposing magnitudes drawn on
 * either half of the shared centre axis.
 *
 * Both values are **magnitudes and must be non-negative** — the side a bar is drawn on encodes its
 * direction, not the sign of its value. A population pyramid passes the male count as [leftValue]
 * and the female count as [rightValue], both positive; a sentiment chart passes the count of
 * negative answers as [leftValue] and positive answers as [rightValue]. This is the key difference
 * from [com.himanshoe.charty.bar.config.NegativeValuesDrawMode] on the ordinary bar charts, where a
 * bar's own sign decides which side of the zero line it grows from and a negative number is a real
 * negative number. Passing a negative value here throws.
 *
 * @property label The category label, drawn in the left gutter alongside the row.
 * @property leftValue The magnitude of the left-hand series. Must be non-negative.
 * @property rightValue The magnitude of the right-hand series. Must be non-negative.
 * @property leftColor Optional colour for this row's left bar; falls back to the chart's left colour.
 * @property rightColor Optional colour for this row's right bar; falls back to the chart's right colour.
 */
@Immutable
data class DivergingData(
    val label: String,
    val leftValue: Float,
    val rightValue: Float,
    val leftColor: ChartyColor? = null,
    val rightColor: ChartyColor? = null,
) {
    init {
        require(leftValue >= 0f) { "leftValue must be non-negative; the side encodes direction, not the sign" }
        require(rightValue >= 0f) { "rightValue must be non-negative; the side encodes direction, not the sign" }
    }
}

/**
 * Which half of a diverging chart a bar belongs to: the series drawn leftwards from the centre axis
 * or the one drawn rightwards.
 */
enum class DivergingSide {
    /** The half drawn from the centre axis towards the left edge of the plot. */
    LEFT,

    /** The half drawn from the centre axis towards the right edge of the plot. */
    RIGHT,
}

/**
 * The half-bar a reader tapped: the row it belongs to and which side of the centre axis it is on.
 *
 * @property data The category row that was tapped.
 * @property side The half of the row that was tapped.
 */
@Immutable
data class DivergingSelection(
    val data: DivergingData,
    val side: DivergingSide,
) {
    /** The magnitude of the tapped half, taken from [data] according to [side]. */
    val value: Float
        get() =
            when (side) {
                DivergingSide.LEFT -> data.leftValue
                DivergingSide.RIGHT -> data.rightValue
            }
}
