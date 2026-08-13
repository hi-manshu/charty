package com.himanshoe.charty.common.config

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor

private const val DEFAULT_BAND_LABEL_PADDING = 4f
private val DEFAULT_BAND_DASH_INTERVALS = floatArrayOf(10f, 10f)

/**
 * Configuration for a **reference band** — a translucent shaded region spanning the area between two
 * values on a chart's numeric axis (for example a "healthy 60–80" zone or an "SLA breach above 200"
 * region). It is the filled-region counterpart of [ReferenceLineConfig] and, unlike a reference
 * line, is drawn *behind* the chart's data so the bars/line remain readable.
 *
 * The band is chart-agnostic: for vertical charts it spans the full width between the two values on
 * the Y axis; for horizontal charts it spans the full height between the two values on the X axis.
 * If the range falls partly outside the visible axis it is clamped; if it falls entirely outside,
 * nothing is drawn. [lowValue] and [highValue] may be given in either order.
 *
 * @property lowValue One edge of the band on the value axis.
 * @property highValue The other edge of the band on the value axis.
 * @property isEnabled Whether the band should be rendered.
 * @property fill The band's fill, solid or gradient, defined by a [ChartyColor].
 * @property fillAlpha Opacity of the fill in the range `[0, 1]`. Kept low by default so data stays
 *   visible through the band.
 * @property borderColor Optional colour (solid or gradient) for lines drawn at the band's two edges.
 *   When `null`, no edge lines are drawn.
 * @property borderWidth Stroke width of the edge lines in pixels. Only used when [borderColor] is
 *   non-null.
 * @property borderStyle Stroke style of the edge lines, solid or dashed.
 * @property dashIntervals On/off lengths of the edge dash pattern in pixels, used when [borderStyle]
 *   is [ReferenceLineStrokeStyle.DASHED].
 * @property label Optional text label drawn at the band's high edge. When `null`, no label is shown.
 * @property labelTextStyle Text style applied to [label].
 * @property labelTextColor Color applied to [labelTextStyle] when drawing, so a gradient can be used;
 *   `null` keeps the color carried by [labelTextStyle] itself.
 * @property labelPadding Inset of the label from the band's corner, in pixels.
 */
@Immutable
data class ReferenceBandConfig(
    val lowValue: Float,
    val highValue: Float,
    val isEnabled: Boolean = true,
    val fill: ChartyColor = ChartyColor.Solid(Color.Red),
    val fillAlpha: Float = 0.15f,
    val borderColor: ChartyColor? = null,
    val borderWidth: Float = 1f,
    val borderStyle: ReferenceLineStrokeStyle = ReferenceLineStrokeStyle.DASHED,
    val dashIntervals: FloatArray = DEFAULT_BAND_DASH_INTERVALS,
    val label: String? = null,
    val labelTextStyle: TextStyle =
        TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black),
    val labelTextColor: ChartyColor? = null,
    val labelPadding: Float = DEFAULT_BAND_LABEL_PADDING,
) {
    init {
        require(fillAlpha in 0f..1f) { "fillAlpha must be between 0 and 1, got: $fillAlpha" }
        require(borderWidth > 0f) { "borderWidth must be positive, got: $borderWidth" }
        require(dashIntervals.isNotEmpty()) { "dashIntervals must not be empty" }
        require(labelPadding >= 0f) { "labelPadding must be non-negative, got: $labelPadding" }
    }

    /**
     * The edge dash pattern as a ready-made [PathEffect], built on first use and then reused so the
     * drawer does not rebuild one on every frame. `null` when [borderStyle] is
     * [ReferenceLineStrokeStyle.SOLID].
     */
    internal val borderPathEffect: PathEffect? by lazy {
        when (borderStyle) {
            ReferenceLineStrokeStyle.SOLID -> null
            ReferenceLineStrokeStyle.DASHED -> PathEffect.dashPathEffect(dashIntervals)
        }
    }
}
