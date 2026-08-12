package com.himanshoe.charty.common.annotation

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor

private const val DEFAULT_ANNOTATION_LINE_WIDTH = 1.5f
private const val DEFAULT_SHAPE_STROKE_WIDTH = 2f
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
 * The geometric primitive drawn by a [ChartAnnotation.Shape].
 */
enum class AnnotationShapeKind {
    /** An axis-aligned rectangle spanning the two anchors. */
    RECT,

    /** An ellipse inscribed in the rectangle spanning the two anchors. */
    ELLIPSE,

    /** A straight line drawn from the first anchor to the second. */
    LINE,
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
 * An overlay drawn on top of a chart's data: a marker line, a text callout bubble, or a geometric
 * shape.
 *
 * Annotations are rendered as a final pass on top of all chart content so they are always visible
 * regardless of the data drawn beneath them. Every subtype resolves its position at draw time, so an
 * annotation stays glued to its data as the chart zooms, pans or streams.
 *
 * Example:
 * ```kotlin
 * ChartAnnotation(xIndex = 5, label = "Peak")
 * ChartAnnotation.Callout(text = "Spike", anchor = AnnotationAnchor.DataPoint(index = 5))
 * ```
 */
sealed interface ChartAnnotation {
    /**
     * A vertical marker line at a data index with an optional text chip pinned to the top or bottom
     * edge of the plot.
     *
     * @property xIndex Zero-based index of the data point where the annotation is placed.
     * @property label Text shown in the annotation label chip. Pass an empty string to draw only the
     *   line with no label.
     * @property style Visual configuration for the line and label.
     */
    data class Marker(
        val xIndex: Int,
        val label: String,
        val style: AnnotationStyle = AnnotationStyle(),
    ) : ChartAnnotation

    /**
     * A rounded speech bubble carrying [text], joined by a connector to the point [anchor] resolves
     * to. The bubble is clamped so it always stays inside the plot bounds.
     *
     * @property text Text rendered inside the bubble.
     * @property anchor Where the connector points; resolved at draw time against the chart's bounds.
     * @property style Visual configuration for the bubble, its text and its connector.
     */
    data class Callout(
        val text: String,
        val anchor: AnnotationAnchor,
        val style: CalloutStyle = CalloutStyle(),
    ) : ChartAnnotation

    /**
     * A geometric highlight spanning two anchors, used to ring or band a region of interest.
     *
     * @property from The first anchor; resolved at draw time against the chart's bounds.
     * @property to The second anchor; resolved at draw time against the chart's bounds.
     * @property kind Which primitive to draw between the two anchors.
     * @property color Stroke color, or fill color when [filled] is `true`.
     * @property strokeWidth Stroke width in pixels; ignored when [filled] is `true`.
     * @property filled Whether the shape is filled instead of outlined. Has no effect on
     *   [AnnotationShapeKind.LINE], which is always stroked.
     */
    data class Shape(
        val from: AnnotationAnchor,
        val to: AnnotationAnchor,
        val kind: AnnotationShapeKind,
        val color: ChartyColor,
        val strokeWidth: Float = DEFAULT_SHAPE_STROKE_WIDTH,
        val filled: Boolean = false,
    ) : ChartAnnotation {
        init {
            require(strokeWidth > 0f) { "strokeWidth must be > 0, was $strokeWidth" }
        }
    }
}

/**
 * Creates a [ChartAnnotation.Marker] — the vertical marker line annotation.
 *
 * @param xIndex Zero-based index of the data point where the annotation is placed.
 * @param label Text shown in the annotation label chip. Pass an empty string to draw only the line.
 * @param style Visual configuration for the line and label.
 * @return The marker annotation.
 */
@Stable
@Suppress("FunctionNaming", "ktlint:standard:function-naming")
fun ChartAnnotation(
    xIndex: Int,
    label: String,
    style: AnnotationStyle = AnnotationStyle(),
): ChartAnnotation = ChartAnnotation.Marker(xIndex = xIndex, label = label, style = style)
