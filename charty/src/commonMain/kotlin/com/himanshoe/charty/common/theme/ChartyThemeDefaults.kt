package com.himanshoe.charty.common.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.withChartyColor
import com.himanshoe.charty.common.annotation.AnnotationStyle
import com.himanshoe.charty.common.brush.BrushSelectionStyle
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.ReferenceBandConfig
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.config.ScrollEdgeFadeConfig
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPadding

/**
 * Theme-derived defaults for chart parameters. Each function reads the ambient [currentChartyTheme],
 * so a chart wrapped in a [ChartyThemeProvider] picks up its styling without the caller repeating it.
 * Use these as the default values of a chart's parameters and of the config properties that carry
 * visual styling — `color`, `scaffoldConfig`, `tooltipConfig`, and the rest.
 *
 * Every factory is a `@Composable` because it resolves the theme in composition, never inside a draw
 * loop, and every result is `remember`ed against the theme so a recomposition does not rebuild it.
 *
 * Calling a factory with the default (unthemed) [ChartyTheme] returns exactly the values the matching
 * config's own constructor defaults produce, so adopting them never changes rendering.
 */
object ChartyThemeDefaults {
    /**
     * A [ChartScaffoldConfig] whose axis color, grid color, label text style, and axis/grid
     * thicknesses come from the ambient [ChartyTheme].
     */
    @Composable
    fun scaffoldConfig(): ChartScaffoldConfig {
        val theme = currentChartyTheme
        return remember(theme) {
            ChartScaffoldConfig(
                axisColor = theme.axisColor,
                gridColor = theme.gridColor,
                axisThickness = theme.dimensions.axisThickness,
                gridThickness = theme.dimensions.gridThickness,
                labelTextStyle = theme.labelTextStyle,
            )
        }
    }

    /**
     * The single-series chart color from the ambient [ChartyTheme] ([ChartyTheme.primaryColor]).
     */
    @Composable
    fun primaryColor(): ChartyColor = currentChartyTheme.primaryColor

    /**
     * The color for series [index] from the ambient [ChartyTheme] palette (see
     * [ChartyTheme.colorForSeries]).
     */
    @Composable
    fun seriesColor(index: Int): ChartyColor = currentChartyTheme.colorForSeries(index)

    /**
     * A [TextStyle] for value labels drawn on the data itself, from the ambient theme's
     * [ChartyTypography.dataLabel] painted with [ChartyComponentColors.dataLabelText].
     */
    @Composable
    fun dataLabelStyle(): TextStyle {
        val theme = currentChartyTheme
        return remember(theme) {
            theme.typography.dataLabel.withChartyColor(theme.componentColors.dataLabelText)
        }
    }

    /**
     * A [TextStyle] for legend entries, from the ambient theme's [ChartyTypography.legendLabel]
     * painted with [ChartyComponentColors.legendLabelText]. When that color role is left unset the
     * style keeps an unspecified color, which makes [com.himanshoe.charty.common.ChartLegend] tint
     * each label like the series it labels.
     */
    @Composable
    fun legendLabelStyle(): TextStyle {
        val theme = currentChartyTheme
        return remember(theme) {
            theme.typography.legendLabel.withChartyColor(theme.componentColors.legendLabelText)
        }
    }

    /**
     * A [TooltipConfig] whose fill, border, text, shape, and metrics come from the ambient
     * [ChartyTheme].
     *
     * @param showArrow Whether the bubble points an arrow at its anchor; pass `false` for the
     *   crosshair-style label that floats free of the point.
     */
    @Composable
    fun tooltipConfig(showArrow: Boolean = true): TooltipConfig {
        val theme = currentChartyTheme
        return remember(theme, showArrow) {
            TooltipConfig(
                shape = theme.shapes.tooltip,
                cornerRadius = theme.shapes.tooltipCornerRadius,
                backgroundColor = theme.componentColors.tooltipBackground,
                borderColor = theme.componentColors.tooltipBorder,
                borderWidth = theme.dimensions.tooltipBorderWidth,
                textStyle = theme.typography.tooltipLabel,
                textColor = theme.componentColors.tooltipText,
                padding =
                    TooltipPadding(
                        horizontal = theme.dimensions.tooltipHorizontalPadding,
                        vertical = theme.dimensions.tooltipVerticalPadding,
                    ),
                elevation = theme.dimensions.tooltipElevation,
                offsetY = theme.dimensions.tooltipOffsetY,
                minDistanceFromEdge = theme.dimensions.tooltipMinDistanceFromEdge,
                showArrow = showArrow,
                arrowSize = theme.dimensions.tooltipArrowSize,
            )
        }
    }

    /**
     * A [ChartCrosshairConfig] whose guide lines, dot, and value bubble come from the ambient
     * [ChartyTheme].
     */
    @Composable
    fun crosshairConfig(): ChartCrosshairConfig {
        val theme = currentChartyTheme
        val tooltip = tooltipConfig(showArrow = false)
        return remember(theme, tooltip) {
            ChartCrosshairConfig(
                verticalLineColor = theme.componentColors.crosshairVerticalLine,
                horizontalLineColor = theme.componentColors.crosshairHorizontalLine,
                lineWidth = theme.dimensions.crosshairLineWidth,
                dotRadius = theme.dimensions.crosshairDotRadius,
                tooltipConfig = tooltip,
            )
        }
    }

    /**
     * A [PersistentMarker] for [dataIndex] whose dot, callout pill, and guide line come from the
     * ambient [ChartyTheme].
     *
     * @param dataIndex Index of the point to mark; negative values count back from the newest point.
     * @param label Callout text; `null` uses the point's formatted value.
     */
    @Composable
    fun markerStyle(
        dataIndex: Int,
        label: String? = null,
    ): PersistentMarker {
        val theme = currentChartyTheme
        return remember(theme, dataIndex, label) {
            PersistentMarker(
                dataIndex = dataIndex,
                label = label,
                dotRadius = theme.dimensions.markerDotRadius,
                dotColor = theme.componentColors.markerDot,
                dotRingColor = theme.componentColors.markerDotRing,
                dotRingWidth = theme.dimensions.markerDotRingWidth,
                labelTextStyle = theme.typography.markerLabel,
                labelTextColor = theme.componentColors.markerLabelText,
                labelBackgroundColor = theme.componentColors.markerLabelBackground,
                labelPadding = theme.dimensions.markerLabelPadding,
                labelCornerRadius = theme.shapes.markerLabelCornerRadius,
                labelGap = theme.dimensions.markerLabelGap,
                guideLineColor = theme.componentColors.markerGuideLine,
                guideLineWidth = theme.dimensions.markerGuideLineWidth,
            )
        }
    }

    /**
     * An [AnnotationStyle] whose line, label chip, and text come from the ambient [ChartyTheme].
     */
    @Composable
    fun annotationStyle(): AnnotationStyle {
        val theme = currentChartyTheme
        return remember(theme) {
            AnnotationStyle(
                lineColor = theme.componentColors.annotationLine,
                lineWidth = theme.dimensions.annotationLineWidth,
                labelBackgroundColor = theme.componentColors.annotationLabelBackground,
                labelTextColor = theme.componentColors.annotationLabelText,
                labelTextStyle = theme.typography.annotationLabel,
                labelHorizontalPadding = theme.dimensions.annotationLabelHorizontalPadding,
                labelVerticalPadding = theme.dimensions.annotationLabelVerticalPadding,
                labelCornerRadius = theme.shapes.annotationLabelCornerRadius,
            )
        }
    }

    /**
     * A [BrushSelectionStyle] whose fill, edge markers, and edge width come from the ambient
     * [ChartyTheme].
     */
    @Composable
    fun brushSelectionStyle(): BrushSelectionStyle {
        val theme = currentChartyTheme
        return remember(theme) {
            BrushSelectionStyle(
                fill = theme.componentColors.selectionFill,
                border = theme.componentColors.selectionBorder,
                edgeWidth = theme.dimensions.selectionEdgeWidth,
            )
        }
    }

    /**
     * A [ReferenceLineConfig] at [value] whose line and label styling come from the ambient
     * [ChartyTheme].
     *
     * @param value Position of the line on the chart's value axis.
     * @param label Text drawn next to the line; `null` falls back to the formatted [value].
     */
    @Composable
    fun referenceLineConfig(
        value: Float,
        label: String? = null,
    ): ReferenceLineConfig {
        val theme = currentChartyTheme
        return remember(theme, value, label) {
            ReferenceLineConfig(
                value = value,
                color = theme.componentColors.referenceLine,
                strokeWidth = theme.dimensions.referenceLineStrokeWidth,
                label = label,
                labelTextStyle = theme.typography.referenceLineLabel,
                labelTextColor = theme.componentColors.referenceLineLabelText,
                labelOffset = theme.dimensions.referenceLineLabelOffset,
            )
        }
    }

    /**
     * A [ReferenceBandConfig] spanning [lowValue] to [highValue] whose fill, edges, and label styling
     * come from the ambient [ChartyTheme].
     *
     * @param lowValue One edge of the band on the value axis.
     * @param highValue The other edge of the band on the value axis.
     * @param label Text drawn at the band's high edge; `null` draws no label.
     */
    @Composable
    fun referenceBandConfig(
        lowValue: Float,
        highValue: Float,
        label: String? = null,
    ): ReferenceBandConfig {
        val theme = currentChartyTheme
        return remember(theme, lowValue, highValue, label) {
            ReferenceBandConfig(
                lowValue = lowValue,
                highValue = highValue,
                fill = theme.componentColors.referenceBandFill,
                fillAlpha = theme.dimensions.referenceBandFillAlpha,
                borderColor = theme.componentColors.referenceBandBorder,
                borderWidth = theme.dimensions.referenceBandBorderWidth,
                label = label,
                labelTextStyle = theme.typography.referenceBandLabel,
                labelTextColor = theme.componentColors.referenceBandLabelText,
                labelPadding = theme.dimensions.referenceBandLabelPadding,
            )
        }
    }

    /**
     * A [ScrollEdgeFadeConfig] whose scrim color, width, and opacity come from the ambient
     * [ChartyTheme].
     */
    @Composable
    fun scrollEdgeFadeConfig(): ScrollEdgeFadeConfig {
        val theme = currentChartyTheme
        return remember(theme) {
            ScrollEdgeFadeConfig(
                width = theme.dimensions.scrollEdgeFadeWidth,
                color = theme.componentColors.scrollEdgeFade,
                alpha = theme.dimensions.scrollEdgeFadeAlpha,
            )
        }
    }
}
