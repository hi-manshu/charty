package com.himanshoe.charty.common.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor

private const val TOOLTIP_BACKGROUND_ARGB = 0xFF2D2D2D
private const val DARK_TOOLTIP_BACKGROUND_ARGB = 0xFF1E1E1E
private const val DARK_TOOLTIP_BORDER_ARGB = 0xFF3A3A3A
private const val MARKER_DOT_ARGB = 0xFF1E88E5
private const val DARK_MARKER_DOT_ARGB = 0xFF64B5F6
private const val MARKER_LABEL_BACKGROUND_ARGB = 0xFF212121
private const val DARK_MARKER_LABEL_BACKGROUND_ARGB = 0xFF2A2A2A
private const val MARKER_GUIDE_LINE_ARGB = 0x552962FF
private const val ANNOTATION_ARGB = 0xFFE53935
private const val DARK_ANNOTATION_ARGB = 0xFFEF5350
private const val SELECTION_ARGB = 0xFF2196F3
private const val DARK_SELECTION_ARGB = 0xFF64B5F6
private const val DARK_REFERENCE_ARGB = 0xFFEF5350
private const val DARK_LABEL_TEXT_ARGB = 0xFFE0E0E0
private const val DARK_SURFACE_ARGB = 0xFF121212

private const val SELECTION_FILL_ALPHA = 0.15f
private const val SELECTION_BORDER_ALPHA = 0.6f
private const val DARK_SELECTION_FILL_ALPHA = 0.18f
private const val DARK_SELECTION_BORDER_ALPHA = 0.7f
private const val CROSSHAIR_VERTICAL_ALPHA = 0.4f
private const val CROSSHAIR_HORIZONTAL_ALPHA = 0.15f

/**
 * Every color Charty paints outside the series themselves: tooltips, crosshair, markers,
 * annotations, brush selection, reference lines and bands, and the scroll-edge scrim.
 *
 * Each role is a [ChartyColor], so a gradient is accepted everywhere a solid color is — including
 * text roles, which are resolved onto their [androidx.compose.ui.text.TextStyle] as a brush (see
 * [com.himanshoe.charty.color.withChartyColor]). A `null` role means "inherit": the crosshair pill
 * falls back to [ChartyTheme.primaryColor], crosshair text to [axisLabelText], and legend text to the
 * series color it labels.
 *
 * Mapping a Material 3 scheme takes a handful of lines:
 *
 * ```kotlin
 * ChartyComponentColors(
 *     tooltipBackground = ChartyColor.Solid(MaterialTheme.colorScheme.inverseSurface),
 *     tooltipText = ChartyColor.Solid(MaterialTheme.colorScheme.inverseOnSurface),
 *     markerDot = ChartyColor.Solid(MaterialTheme.colorScheme.primary),
 *     annotationLine = ChartyColor.Solid(MaterialTheme.colorScheme.error),
 * )
 * ```
 *
 * @property axisLabelText Color of the axis tick labels.
 * @property dataLabelText Color of the value labels drawn on the data itself.
 * @property tooltipBackground Fill of the tooltip bubble.
 * @property tooltipBorder Outline of the tooltip bubble; `null` draws no border.
 * @property tooltipText Color of the tooltip's text.
 * @property crosshairVerticalLine Color of the crosshair's vertical guide line.
 * @property crosshairHorizontalLine Color of the crosshair's horizontal guide line.
 * @property crosshairLabelBackground Fill of the crosshair's value pill; `null` uses
 *   [ChartyTheme.primaryColor].
 * @property crosshairLabelText Color of the crosshair pill's text; `null` uses [axisLabelText].
 * @property markerDot Fill of a persistent marker's dot.
 * @property markerDotRing Ring drawn around a marker's dot for contrast; `null` draws no ring.
 * @property markerLabelBackground Fill of a marker's callout pill.
 * @property markerLabelText Color of a marker callout's text.
 * @property markerGuideLine Color of the guide line dropped from a marker to the axis.
 * @property annotationLine Color of an annotation's vertical line.
 * @property annotationLabelBackground Fill of an annotation's label chip.
 * @property annotationLabelText Color of an annotation label's text.
 * @property selectionFill Fill of the brush-selection rectangle.
 * @property selectionBorder Color of the brush-selection edge markers.
 * @property referenceLine Color of a reference line.
 * @property referenceLineLabelText Color of a reference line's label text.
 * @property referenceBandFill Fill of a reference band.
 * @property referenceBandBorder Color of a reference band's edge lines; `null` draws none.
 * @property referenceBandLabelText Color of a reference band's label text.
 * @property scrollEdgeFade Color the scroll-edge scrim fades from; match the chart background.
 * @property legendLabelText Color of legend labels; `null` colors each label like its series dot.
 */
@Immutable
data class ChartyComponentColors(
    val axisLabelText: ChartyColor = ChartyColor.Solid(Color.Black),
    val dataLabelText: ChartyColor = ChartyColor.Solid(Color.Black),
    val tooltipBackground: ChartyColor = ChartyColor.Solid(Color(TOOLTIP_BACKGROUND_ARGB)),
    val tooltipBorder: ChartyColor? = null,
    val tooltipText: ChartyColor = ChartyColor.Solid(Color.White),
    val crosshairVerticalLine: ChartyColor = ChartyColor.Solid(Color.Black.copy(alpha = CROSSHAIR_VERTICAL_ALPHA)),
    val crosshairHorizontalLine: ChartyColor = ChartyColor.Solid(Color.Black.copy(alpha = CROSSHAIR_HORIZONTAL_ALPHA)),
    val crosshairLabelBackground: ChartyColor? = null,
    val crosshairLabelText: ChartyColor? = null,
    val markerDot: ChartyColor = ChartyColor.Solid(Color(MARKER_DOT_ARGB)),
    val markerDotRing: ChartyColor? = ChartyColor.Solid(Color.White),
    val markerLabelBackground: ChartyColor = ChartyColor.Solid(Color(MARKER_LABEL_BACKGROUND_ARGB)),
    val markerLabelText: ChartyColor = ChartyColor.Solid(Color.White),
    val markerGuideLine: ChartyColor = ChartyColor.Solid(Color(MARKER_GUIDE_LINE_ARGB)),
    val annotationLine: ChartyColor = ChartyColor.Solid(Color(ANNOTATION_ARGB)),
    val annotationLabelBackground: ChartyColor = ChartyColor.Solid(Color(ANNOTATION_ARGB)),
    val annotationLabelText: ChartyColor = ChartyColor.Solid(Color.White),
    val selectionFill: ChartyColor = ChartyColor.Solid(Color(SELECTION_ARGB).copy(alpha = SELECTION_FILL_ALPHA)),
    val selectionBorder: ChartyColor = ChartyColor.Solid(Color(SELECTION_ARGB).copy(alpha = SELECTION_BORDER_ALPHA)),
    val referenceLine: ChartyColor = ChartyColor.Solid(Color.Red),
    val referenceLineLabelText: ChartyColor = ChartyColor.Solid(Color.Black),
    val referenceBandFill: ChartyColor = ChartyColor.Solid(Color.Red),
    val referenceBandBorder: ChartyColor? = null,
    val referenceBandLabelText: ChartyColor = ChartyColor.Solid(Color.Black),
    val scrollEdgeFade: ChartyColor = ChartyColor.Solid(Color.White),
    val legendLabelText: ChartyColor? = null,
) {
    /** Light and dark presets for the component colors. */
    companion object {
        /**
         * The light-mode component colors. These are exactly the values Charty hardcoded before the
         * theme covered them, so an app that supplies no theme renders unchanged.
         */
        fun light(): ChartyComponentColors = ChartyComponentColors()

        /**
         * The dark-mode component colors: lighter text and accents, a dark tooltip and scrim, and a
         * marker ring that reads against a dark surface.
         */
        fun dark(): ChartyComponentColors =
            ChartyComponentColors(
                axisLabelText = ChartyColor.Solid(Color(DARK_LABEL_TEXT_ARGB)),
                dataLabelText = ChartyColor.Solid(Color(DARK_LABEL_TEXT_ARGB)),
                tooltipBackground = ChartyColor.Solid(Color(DARK_TOOLTIP_BACKGROUND_ARGB)),
                tooltipBorder = ChartyColor.Solid(Color(DARK_TOOLTIP_BORDER_ARGB)),
                crosshairVerticalLine = ChartyColor.Solid(Color.White.copy(alpha = CROSSHAIR_VERTICAL_ALPHA)),
                crosshairHorizontalLine = ChartyColor.Solid(Color.White.copy(alpha = CROSSHAIR_HORIZONTAL_ALPHA)),
                markerDot = ChartyColor.Solid(Color(DARK_MARKER_DOT_ARGB)),
                markerDotRing = ChartyColor.Solid(Color(DARK_SURFACE_ARGB)),
                markerLabelBackground = ChartyColor.Solid(Color(DARK_MARKER_LABEL_BACKGROUND_ARGB)),
                annotationLine = ChartyColor.Solid(Color(DARK_ANNOTATION_ARGB)),
                annotationLabelBackground = ChartyColor.Solid(Color(DARK_ANNOTATION_ARGB)),
                selectionFill =
                    ChartyColor.Solid(Color(DARK_SELECTION_ARGB).copy(alpha = DARK_SELECTION_FILL_ALPHA)),
                selectionBorder =
                    ChartyColor.Solid(Color(DARK_SELECTION_ARGB).copy(alpha = DARK_SELECTION_BORDER_ALPHA)),
                referenceLine = ChartyColor.Solid(Color(DARK_REFERENCE_ARGB)),
                referenceLineLabelText = ChartyColor.Solid(Color(DARK_LABEL_TEXT_ARGB)),
                referenceBandFill = ChartyColor.Solid(Color(DARK_REFERENCE_ARGB)),
                referenceBandLabelText = ChartyColor.Solid(Color(DARK_LABEL_TEXT_ARGB)),
                scrollEdgeFade = ChartyColor.Solid(Color(DARK_SURFACE_ARGB)),
            )
    }
}
