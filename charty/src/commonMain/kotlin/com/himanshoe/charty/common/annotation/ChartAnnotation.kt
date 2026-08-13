package com.himanshoe.charty.common.annotation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor

private const val DEFAULT_ANNOTATION_LINE_WIDTH = 1.5f
private const val DEFAULT_LABEL_TEXT_SP = 11
private const val DEFAULT_LABEL_HORIZONTAL_PADDING = 8f
private const val DEFAULT_LABEL_VERTICAL_PADDING = 4f
private const val DEFAULT_LABEL_CORNER_RADIUS = 4f
private val DEFAULT_ANNOTATION_COLOR = Color(0xFFE53935)
private val DEFAULT_DASH_INTERVALS = floatArrayOf(6f, 3f)

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
 * Visual style for a [ChartAnnotation]. Build one from the ambient theme with
 * [com.himanshoe.charty.common.theme.ChartyThemeDefaults.annotationStyle].
 *
 * @property lineColor Color of the vertical annotation line, solid or gradient.
 * @property lineWidth Stroke width of the annotation line in pixels.
 * @property isDashed Whether to draw the line as a dashed pattern.
 * @property dashIntervals On/off lengths of the dash pattern in pixels, used when [isDashed] is `true`.
 * @property labelBackgroundColor Background fill of the label chip, solid or gradient.
 * @property labelTextColor Text color inside the label chip, solid or gradient.
 * @property labelTextStyle Font attributes of the label text; its own color is replaced by
 *   [labelTextColor].
 * @property labelHorizontalPadding Horizontal padding between the label text and the chip edge, in pixels.
 * @property labelVerticalPadding Vertical padding between the label text and the chip edge, in pixels.
 * @property labelCornerRadius Corner radius of the label chip, in pixels.
 * @property labelPosition Whether the chip appears at [AnnotationLabelPosition.TOP] or [AnnotationLabelPosition.BOTTOM].
 */
@Immutable
data class AnnotationStyle(
    val lineColor: ChartyColor = ChartyColor.Solid(DEFAULT_ANNOTATION_COLOR),
    val lineWidth: Float = DEFAULT_ANNOTATION_LINE_WIDTH,
    val isDashed: Boolean = true,
    val dashIntervals: FloatArray = DEFAULT_DASH_INTERVALS,
    val labelBackgroundColor: ChartyColor = ChartyColor.Solid(DEFAULT_ANNOTATION_COLOR),
    val labelTextColor: ChartyColor = ChartyColor.Solid(Color.White),
    val labelTextStyle: TextStyle = TextStyle(fontSize = DEFAULT_LABEL_TEXT_SP.sp),
    val labelHorizontalPadding: Float = DEFAULT_LABEL_HORIZONTAL_PADDING,
    val labelVerticalPadding: Float = DEFAULT_LABEL_VERTICAL_PADDING,
    val labelCornerRadius: Float = DEFAULT_LABEL_CORNER_RADIUS,
    val labelPosition: AnnotationLabelPosition = AnnotationLabelPosition.TOP,
) {
    init {
        require(lineWidth >= 0f) { "Annotation lineWidth must be non-negative" }
        require(dashIntervals.isNotEmpty()) { "Annotation dashIntervals must not be empty" }
        require(labelHorizontalPadding >= 0f) { "Annotation labelHorizontalPadding must be non-negative" }
        require(labelVerticalPadding >= 0f) { "Annotation labelVerticalPadding must be non-negative" }
        require(labelCornerRadius >= 0f) { "Annotation labelCornerRadius must be non-negative" }
    }

    /**
     * The dash pattern as a ready-made [PathEffect], built on first use and then reused so the
     * drawer does not rebuild one on every frame. `null` when [isDashed] is `false`.
     */
    internal val dashPathEffect: PathEffect? by lazy {
        if (isDashed) {
            PathEffect.dashPathEffect(dashIntervals)
        } else {
            null
        }
    }
}

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
 * ChartAnnotation(xIndex = 5, label = "Peak", style = AnnotationStyle(lineColor = ChartyColor.Solid(Color.Red)))
 * ```
 */
@Immutable
data class ChartAnnotation(
    val xIndex: Int,
    val label: String,
    val style: AnnotationStyle = AnnotationStyle(),
)
