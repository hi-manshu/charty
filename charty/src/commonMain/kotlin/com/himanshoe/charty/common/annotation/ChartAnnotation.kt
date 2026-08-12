package com.himanshoe.charty.common.annotation

import androidx.compose.ui.graphics.Color

private const val DEFAULT_ANNOTATION_LINE_WIDTH = 1.5f
private val DEFAULT_ANNOTATION_COLOR = Color(0xFFE53935)

/**
 * Specifies where the annotation label is drawn relative to the chart.
 */
enum class AnnotationLabelPosition {
    /** Label anchored to the top edge of the chart. */
    TOP,

    /** Label anchored to the bottom edge of the chart. */
    BOTTOM,
}

/**
 * Visual style for a [ChartAnnotation].
 *
 * @property lineColor Color of the vertical annotation line.
 * @property lineWidth Stroke width of the annotation line in pixels.
 * @property isDashed Whether to draw the line as a dashed pattern.
 * @property labelBackgroundColor Background fill of the label chip.
 * @property labelTextColor Text color inside the label chip.
 * @property labelPosition Whether the chip appears at [AnnotationLabelPosition.TOP] or [AnnotationLabelPosition.BOTTOM].
 */
data class AnnotationStyle(
    val lineColor: Color = DEFAULT_ANNOTATION_COLOR,
    val lineWidth: Float = DEFAULT_ANNOTATION_LINE_WIDTH,
    val isDashed: Boolean = true,
    val labelBackgroundColor: Color = DEFAULT_ANNOTATION_COLOR,
    val labelTextColor: Color = Color.White,
    val labelPosition: AnnotationLabelPosition = AnnotationLabelPosition.TOP,
)

/**
 * An annotation that overlays a vertical marker line and a text label on a chart.
 *
 * Annotations are rendered as a final pass on top of all chart content so they are
 * always visible regardless of the data drawn beneath them.
 *
 * @property xIndex Zero-based index of the data point where the annotation is placed.
 * @property label Text shown in the annotation label chip. Pass an empty string to draw
 *   only the line with no label.
 * @property style Visual configuration for the line and label.
 *
 * Example:
 * ```kotlin
 * ChartAnnotation(xIndex = 5, label = "Peak", style = AnnotationStyle(lineColor = Color.Red))
 * ```
 */
data class ChartAnnotation(
    val xIndex: Int,
    val label: String,
    val style: AnnotationStyle = AnnotationStyle(),
)
