package com.himanshoe.charty.common.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private const val AXIS_LABEL_SP = 12
private const val DATA_LABEL_SP = 12
private const val TOOLTIP_LABEL_SP = 14
private const val MARKER_LABEL_SP = 11
private const val CROSSHAIR_LABEL_SP = 12
private const val ANNOTATION_LABEL_SP = 11
private const val REFERENCE_LINE_LABEL_SP = 12
private const val REFERENCE_BAND_LABEL_SP = 11

/**
 * The text roles Charty draws, as font attributes only — size, weight, family, letter spacing.
 *
 * Text **color** deliberately does not live here: it is a [com.himanshoe.charty.color.ChartyColor] on
 * [ChartyComponentColors] so that a caller can pass a gradient, which Charty resolves onto the style
 * as a brush via [com.himanshoe.charty.color.withChartyColor]. Any color carried by the styles below
 * is therefore only a fallback for callers who bypass the theme.
 *
 * Mapping an existing design system is one line per role:
 *
 * ```kotlin
 * ChartyTypography(
 *     axisLabel = MaterialTheme.typography.labelSmall,
 *     dataLabel = MaterialTheme.typography.labelSmall,
 *     tooltipLabel = MaterialTheme.typography.bodySmall,
 *     legendLabel = MaterialTheme.typography.labelMedium,
 * )
 * ```
 *
 * @property axisLabel Style of the axis tick labels drawn by the chart scaffold.
 * @property dataLabel Style of the value labels drawn next to bars, points, and line vertices.
 * @property tooltipLabel Style of the text inside a tooltip bubble.
 * @property legendLabel Style of the series labels in a [com.himanshoe.charty.common.ChartLegend].
 * @property markerLabel Style of a [com.himanshoe.charty.common.config.PersistentMarker] callout.
 * @property crosshairLabel Style of the crosshair's value pill.
 * @property annotationLabel Style of a [com.himanshoe.charty.common.annotation.ChartAnnotation] chip.
 * @property referenceLineLabel Style of a reference line's label.
 * @property referenceBandLabel Style of a reference band's label.
 */
@Immutable
data class ChartyTypography(
    val axisLabel: TextStyle = TextStyle(fontSize = AXIS_LABEL_SP.sp),
    val dataLabel: TextStyle = TextStyle(fontSize = DATA_LABEL_SP.sp),
    val tooltipLabel: TextStyle = TextStyle(fontSize = TOOLTIP_LABEL_SP.sp),
    val legendLabel: TextStyle = TextStyle.Default,
    val markerLabel: TextStyle = TextStyle(fontSize = MARKER_LABEL_SP.sp, fontWeight = FontWeight.SemiBold),
    val crosshairLabel: TextStyle = TextStyle(fontSize = CROSSHAIR_LABEL_SP.sp, fontWeight = FontWeight.SemiBold),
    val annotationLabel: TextStyle = TextStyle(fontSize = ANNOTATION_LABEL_SP.sp),
    val referenceLineLabel: TextStyle = TextStyle(fontSize = REFERENCE_LINE_LABEL_SP.sp, fontWeight = FontWeight.Bold),
    val referenceBandLabel: TextStyle = TextStyle(fontSize = REFERENCE_BAND_LABEL_SP.sp, fontWeight = FontWeight.Bold),
) {
    /** The single set of Charty text roles; fonts do not differ between light and dark. */
    companion object {
        /**
         * The built-in text roles, matching the sizes and weights Charty has always drawn with.
         */
        fun default(): ChartyTypography = ChartyTypography()
    }
}
