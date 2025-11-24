package com.himanshoe.charty.common.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Stroke style for reference/target/average lines in charts.
 */
enum class ReferenceLineStrokeStyle {
    SOLID,
    DASHED,
}

/**
 * Position of the label relative to the reference line.
 */
enum class ReferenceLineLabelPosition {
    START,
    CENTER,
    END,
    ABOVE,
    BELOW,
}

/**
 * Configuration for a single reference (target/average) line in a chart.
 *
 * This configuration is chart-agnostic and can be used with any chart type
 * that has a numeric domain (e.g., bar, line, point, combo, pie-as-percentage).
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
