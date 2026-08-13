package com.himanshoe.charty.common.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val AXIS_THICKNESS_PX = 2f
private const val GRID_THICKNESS_PX = 1f
private const val MARKER_DOT_RADIUS_PX = 6f
private const val MARKER_DOT_RING_WIDTH_PX = 2f
private const val MARKER_LABEL_PADDING_PX = 6f
private const val MARKER_LABEL_GAP_PX = 6f
private const val MARKER_GUIDE_LINE_WIDTH_PX = 1f
private const val ANNOTATION_LINE_WIDTH_PX = 1.5f
private const val ANNOTATION_LABEL_HORIZONTAL_PADDING_PX = 8f
private const val ANNOTATION_LABEL_VERTICAL_PADDING_PX = 4f
private const val CROSSHAIR_LINE_WIDTH_PX = 1.5f
private const val CROSSHAIR_DOT_RADIUS_PX = 8f
private const val REFERENCE_LINE_STROKE_WIDTH_PX = 2f
private const val REFERENCE_LINE_LABEL_OFFSET_PX = 4f
private const val REFERENCE_BAND_BORDER_WIDTH_PX = 1f
private const val REFERENCE_BAND_LABEL_PADDING_PX = 4f
private const val REFERENCE_BAND_FILL_ALPHA = 0.15f
private const val SELECTION_EDGE_WIDTH_PX = 2f
private const val SCROLL_EDGE_FADE_WIDTH_PX = 32f
private const val SCROLL_EDGE_FADE_ALPHA = 0.9f

/**
 * The sizes and spacings Charty draws its overlays with — line thicknesses, radii, paddings, and the
 * tooltip's offsets and elevation.
 *
 * Values expressed in [Dp] belong to surfaces laid out in Compose or converted by their drawer;
 * values expressed as [Float] are device pixels used directly inside a canvas drawer, matching the
 * pixel-typed properties on the configs they feed.
 *
 * @property axisThickness Stroke width of the axis lines, in pixels.
 * @property gridThickness Stroke width of the grid lines, in pixels.
 * @property tooltipBorderWidth Stroke width of the tooltip outline.
 * @property tooltipElevation Shadow elevation of the tooltip bubble.
 * @property tooltipOffsetY Gap between the anchored point and the tooltip bubble.
 * @property tooltipMinDistanceFromEdge Smallest gap the tooltip keeps from the chart edges.
 * @property tooltipArrowSize Size of the tooltip's pointer arrow.
 * @property tooltipHorizontalPadding Horizontal padding between tooltip text and bubble edge.
 * @property tooltipVerticalPadding Vertical padding between tooltip text and bubble edge.
 * @property markerDotRadius Radius of a persistent marker's dot, in pixels.
 * @property markerDotRingWidth Stroke width of a marker dot's contrast ring, in pixels.
 * @property markerLabelPadding Padding inside a marker's callout pill, in pixels.
 * @property markerLabelGap Gap between a marker's dot and its callout pill, in pixels.
 * @property markerGuideLineWidth Stroke width of a marker's guide line, in pixels.
 * @property annotationLineWidth Stroke width of an annotation's line, in pixels.
 * @property annotationLabelHorizontalPadding Horizontal padding inside an annotation chip, in pixels.
 * @property annotationLabelVerticalPadding Vertical padding inside an annotation chip, in pixels.
 * @property crosshairLineWidth Stroke width of the crosshair guide lines, in pixels.
 * @property crosshairDotRadius Radius of the crosshair's highlight dot, in pixels.
 * @property referenceLineStrokeWidth Stroke width of a reference line, in pixels.
 * @property referenceLineLabelOffset Gap between a reference line and its label, in pixels.
 * @property referenceBandBorderWidth Stroke width of a reference band's edge lines, in pixels.
 * @property referenceBandLabelPadding Inset of a reference band's label from the band corner, in pixels.
 * @property referenceBandFillAlpha Opacity of a reference band's fill, in `[0, 1]`.
 * @property selectionEdgeWidth Stroke width of the brush-selection edge markers, in pixels.
 * @property scrollEdgeFadeWidth Width of the scroll-edge scrim, in pixels.
 * @property scrollEdgeFadeAlpha Opacity of the scroll-edge scrim at the edge, in `[0, 1]`.
 */
@Immutable
data class ChartyDimensions(
    val axisThickness: Float = AXIS_THICKNESS_PX,
    val gridThickness: Float = GRID_THICKNESS_PX,
    val tooltipBorderWidth: Dp = 1.dp,
    val tooltipElevation: Dp = 4.dp,
    val tooltipOffsetY: Dp = 8.dp,
    val tooltipMinDistanceFromEdge: Dp = 16.dp,
    val tooltipArrowSize: Dp = 8.dp,
    val tooltipHorizontalPadding: Dp = 12.dp,
    val tooltipVerticalPadding: Dp = 8.dp,
    val markerDotRadius: Float = MARKER_DOT_RADIUS_PX,
    val markerDotRingWidth: Float = MARKER_DOT_RING_WIDTH_PX,
    val markerLabelPadding: Float = MARKER_LABEL_PADDING_PX,
    val markerLabelGap: Float = MARKER_LABEL_GAP_PX,
    val markerGuideLineWidth: Float = MARKER_GUIDE_LINE_WIDTH_PX,
    val annotationLineWidth: Float = ANNOTATION_LINE_WIDTH_PX,
    val annotationLabelHorizontalPadding: Float = ANNOTATION_LABEL_HORIZONTAL_PADDING_PX,
    val annotationLabelVerticalPadding: Float = ANNOTATION_LABEL_VERTICAL_PADDING_PX,
    val crosshairLineWidth: Float = CROSSHAIR_LINE_WIDTH_PX,
    val crosshairDotRadius: Float = CROSSHAIR_DOT_RADIUS_PX,
    val referenceLineStrokeWidth: Float = REFERENCE_LINE_STROKE_WIDTH_PX,
    val referenceLineLabelOffset: Float = REFERENCE_LINE_LABEL_OFFSET_PX,
    val referenceBandBorderWidth: Float = REFERENCE_BAND_BORDER_WIDTH_PX,
    val referenceBandLabelPadding: Float = REFERENCE_BAND_LABEL_PADDING_PX,
    val referenceBandFillAlpha: Float = REFERENCE_BAND_FILL_ALPHA,
    val selectionEdgeWidth: Float = SELECTION_EDGE_WIDTH_PX,
    val scrollEdgeFadeWidth: Float = SCROLL_EDGE_FADE_WIDTH_PX,
    val scrollEdgeFadeAlpha: Float = SCROLL_EDGE_FADE_ALPHA,
) {
    init {
        require(axisThickness >= 0f) { "axisThickness must be non-negative" }
        require(gridThickness >= 0f) { "gridThickness must be non-negative" }
        require(markerDotRadius >= 0f) { "markerDotRadius must be non-negative" }
        require(crosshairDotRadius >= 0f) { "crosshairDotRadius must be non-negative" }
        require(referenceLineStrokeWidth > 0f) { "referenceLineStrokeWidth must be positive" }
        require(referenceBandBorderWidth > 0f) { "referenceBandBorderWidth must be positive" }
        require(referenceBandFillAlpha in 0f..1f) { "referenceBandFillAlpha must be between 0 and 1" }
        require(scrollEdgeFadeWidth >= 0f) { "scrollEdgeFadeWidth must be non-negative" }
        require(scrollEdgeFadeAlpha in 0f..1f) { "scrollEdgeFadeAlpha must be between 0 and 1" }
    }

    /** The single set of Charty metrics; sizing does not differ between light and dark. */
    companion object {
        /** The built-in metrics, matching the sizes Charty has always drawn with. */
        fun default(): ChartyDimensions = ChartyDimensions()
    }
}
