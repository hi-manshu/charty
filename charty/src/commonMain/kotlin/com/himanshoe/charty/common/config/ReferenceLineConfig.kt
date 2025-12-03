package com.himanshoe.charty.common.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * An enum that defines the stroke style for reference, target, or average lines in a chart.
 */
enum class ReferenceLineStrokeStyle {
    /** A solid, continuous line. */
    SOLID,

    /** A dashed line, composed of segments. */
    DASHED,
}

/**
 * An enum that defines the position of a label relative to its corresponding reference line.
 */
enum class ReferenceLineLabelPosition {
    /** The label is positioned at the start of the line. */
    START,

    /** The label is positioned at the center of the line. */
    CENTER,

    /** The label is positioned at the end of the line. */
    END,

    /** The label is positioned above the line. */
    ABOVE,

    /** The label is positioned below the line. */
    BELOW,
}

/**
 * A data class that holds the configuration for a single reference line in a chart, such as a target or average line.
 *
 * This configuration is chart-agnostic and can be used with any chart type that has a numeric domain (e.g., bar, line, point, combo).
 *
 * @property isEnabled Determines whether the reference line should be rendered.
 * @property value The value on the chart's numeric axis where the line should be drawn.
 * @property color The color of the reference line.
 * @property strokeWidth The thickness of the reference line in pixels.
 * @property strokeStyle The stroke style of the line, either [ReferenceLineStrokeStyle.SOLID] or [ReferenceLineStrokeStyle.DASHED].
 * @property dashIntervals An optional array of floats for creating a custom dash pattern. If `null` and [strokeStyle] is [ReferenceLineStrokeStyle.DASHED], a default pattern is used.
 * @property label An optional text label to be displayed near the line. If `null`, the chart may show the numeric [value] or nothing.
 * @property showValueInLabelWhenNoText If `true`, the numeric [value] will be shown in the label when [label] is `null`.
 * @property labelTextStyle The [TextStyle] for the label.
 * @property labelPosition The position of the label relative to the line, defined by [ReferenceLineLabelPosition].
 * @property labelOffset The offset in pixels to be applied to the label's position, moving it away from the line.
 */
data class ReferenceLineConfig(
    /**
     * Whether this reference line should be rendered.
     */
    val isEnabled: Boolean = true,
    /**
     * The logical value on the chart's numeric axis/domain where the line should be drawn.
     * For cartesian charts this is typically the value axis (Y for vertical, X for horizontal).
     * For pie charts this can represent a percentage (0-100) or other agreed domain.
     */
    val value: Float,
    /**
     * Color of the reference line.
     */
    val color: Color = Color.Red,
    /**
     * Thickness of the reference line stroke in pixels.
     */
    val strokeWidth: Float = 2f,
    /**
     * Stroke style of the reference line (solid or dashed).
     */
    val strokeStyle: ReferenceLineStrokeStyle = ReferenceLineStrokeStyle.DASHED,
    /**
     * Optional custom dash pattern for dashed lines.
     * If null and [strokeStyle] == DASHED, a sensible default pattern is used.
     */
    val dashIntervals: FloatArray? = null,
    /**
     * Optional explicit label text to render near the line.
     * If null, the chart may choose to show the numeric [value] or nothing.
     */
    val label: String? = null,
    /**
     * Whether charts are allowed to show the numeric [value] in the label
     * when [label] is null. The exact formatting is chart-specific.
     */
    val showValueInLabelWhenNoText: Boolean = true,
    /**
     * Text style for the label.
     */
    val labelTextStyle: TextStyle =
        TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        ),
    /**
     * Position of the label relative to the line.
     */
    val labelPosition: ReferenceLineLabelPosition = ReferenceLineLabelPosition.ABOVE,
    /**
     * Offset in pixels applied to label position away from the line.
     */
    val labelOffset: Float = 4f,
) {
    init {
        require(strokeWidth > 0f) { "strokeWidth must be positive, got: $strokeWidth" }
        require(labelOffset >= 0f) { "labelOffset must be non-negative, got: $labelOffset" }
    }
}
