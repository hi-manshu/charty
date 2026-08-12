package com.himanshoe.charty.common.config

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor

private const val DEFAULT_DOT_ARGB = 0xFF1E88E5
private const val DEFAULT_LABEL_BACKGROUND_ARGB = 0xFF212121
private const val DEFAULT_GUIDE_LINE_ARGB = 0x552962FF

/**
 * A marker pinned to a single data point that stays drawn at all times, independent of touch
 * interaction (unlike the transient crosshair or tooltip). Use it to permanently flag a point of
 * interest — a peak, a target, "today" — with an emphasized dot, an optional callout label, and an
 * optional guide line down to the axis.
 *
 * Pass a list of these to a chart's config (e.g. `LineChartConfig.markers`); an empty list draws
 * nothing.
 *
 * @property dataIndex Zero-based index of the point to mark. A negative index counts back from the
 *   end of the drawn data, so `-1` marks the newest point — the way to keep a label pinned to the
 *   latest value of a rolling `visibleWindow`. Markers whose index falls outside the currently
 *   drawn data are skipped.
 * @property label Text shown in the callout. When `null`, the point's formatted value is used.
 * @property showDot Whether to draw the emphasized dot on the point.
 * @property dotRadius Radius of the dot, in pixels.
 * @property dotColor Fill color (or gradient) of the dot.
 * @property dotRingColor Optional ring drawn around the dot for contrast against the line; `null`
 *   draws no ring.
 * @property dotRingWidth Stroke width of the ring, in pixels.
 * @property showLabel Whether to draw the callout label above the dot.
 * @property labelTextStyle Text style for the callout label.
 * @property labelBackgroundColor Background fill (or gradient) of the callout pill.
 * @property labelPadding Padding between the label text and the pill edges, in pixels.
 * @property labelCornerRadius Corner radius of the callout pill, in pixels.
 * @property showGuideLine Whether to drop a thin guide line from the point to the value axis.
 * @property guideLineColor Color of the guide line.
 * @property guideLineWidth Stroke width of the guide line, in pixels.
 */
@Immutable
data class PersistentMarker(
    val dataIndex: Int,
    val label: String? = null,
    val showDot: Boolean = true,
    val dotRadius: Float = 6f,
    val dotColor: ChartyColor = ChartyColor.Solid(Color(DEFAULT_DOT_ARGB)),
    val dotRingColor: Color? = Color.White,
    val dotRingWidth: Float = 2f,
    val showLabel: Boolean = true,
    val labelTextStyle: TextStyle =
        TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        ),
    val labelBackgroundColor: ChartyColor = ChartyColor.Solid(Color(DEFAULT_LABEL_BACKGROUND_ARGB)),
    val labelPadding: Float = 6f,
    val labelCornerRadius: Float = 6f,
    val showGuideLine: Boolean = false,
    val guideLineColor: Color = Color(DEFAULT_GUIDE_LINE_ARGB),
    val guideLineWidth: Float = 1f,
) {
    init {
        require(dataIndex != Int.MIN_VALUE) { "Marker dataIndex must be a resolvable index" }
        require(dotRadius >= 0f) { "Marker dotRadius must be non-negative" }
        require(dotRingWidth >= 0f) { "Marker dotRingWidth must be non-negative" }
        require(labelPadding >= 0f) { "Marker labelPadding must be non-negative" }
        require(labelCornerRadius >= 0f) { "Marker labelCornerRadius must be non-negative" }
        require(guideLineWidth >= 0f) { "Marker guideLineWidth must be non-negative" }
    }
}
