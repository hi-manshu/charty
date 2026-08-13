package com.himanshoe.charty.common.theme

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
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Guards the promise that the light theme carries exactly the values Charty hardcoded before the
 * theme covered these surfaces: a caller who supplies no theme must render identically to before.
 */
class ChartyThemeParityTest {
    private val theme = ChartyTheme.light()

    @Test
    fun scaffoldDefaults_matchLightTheme() {
        val config = ChartScaffoldConfig()
        assertEquals(config.axisColor, theme.axisColor)
        assertEquals(config.gridColor, theme.gridColor)
        assertEquals(config.axisThickness, theme.dimensions.axisThickness)
        assertEquals(config.gridThickness, theme.dimensions.gridThickness)
        assertEquals(config.labelTextStyle, theme.labelTextStyle)
    }

    @Test
    fun tooltipDefaults_matchLightTheme() {
        val config = TooltipConfig()
        assertEquals(config.backgroundColor, theme.componentColors.tooltipBackground)
        assertEquals(config.borderColor, theme.componentColors.tooltipBorder)
        assertEquals(config.borderWidth, theme.dimensions.tooltipBorderWidth)
        assertEquals(config.elevation, theme.dimensions.tooltipElevation)
        assertEquals(config.offsetY, theme.dimensions.tooltipOffsetY)
        assertEquals(config.minDistanceFromEdge, theme.dimensions.tooltipMinDistanceFromEdge)
        assertEquals(config.arrowSize, theme.dimensions.tooltipArrowSize)
        assertEquals(config.cornerRadius, theme.shapes.tooltipCornerRadius)
        assertEquals(config.padding.horizontal, theme.dimensions.tooltipHorizontalPadding)
        assertEquals(config.padding.vertical, theme.dimensions.tooltipVerticalPadding)
    }

    @Test
    fun tooltipTextDefault_matchesThemedTextStyle() {
        val config = TooltipConfig()
        val themed = theme.typography.tooltipLabel.withChartyColor(theme.componentColors.tooltipText)
        assertEquals(config.textStyle.withChartyColor(config.textColor), themed)
    }

    @Test
    fun crosshairDefaults_matchLightTheme() {
        val config = ChartCrosshairConfig()
        assertEquals(config.verticalLineColor, theme.componentColors.crosshairVerticalLine)
        assertEquals(config.horizontalLineColor, theme.componentColors.crosshairHorizontalLine)
        assertEquals(config.lineWidth, theme.dimensions.crosshairLineWidth)
        assertEquals(config.dotRadius, theme.dimensions.crosshairDotRadius)
    }

    @Test
    fun markerDefaults_matchLightTheme() {
        val marker = PersistentMarker(dataIndex = 0)
        assertEquals(marker.dotColor, theme.componentColors.markerDot)
        assertEquals(marker.dotRingColor, theme.componentColors.markerDotRing)
        assertEquals(marker.labelBackgroundColor, theme.componentColors.markerLabelBackground)
        assertEquals(marker.guideLineColor, theme.componentColors.markerGuideLine)
        assertEquals(marker.dotRadius, theme.dimensions.markerDotRadius)
        assertEquals(marker.dotRingWidth, theme.dimensions.markerDotRingWidth)
        assertEquals(marker.labelPadding, theme.dimensions.markerLabelPadding)
        assertEquals(marker.labelGap, theme.dimensions.markerLabelGap)
        assertEquals(marker.guideLineWidth, theme.dimensions.markerGuideLineWidth)
        assertEquals(marker.labelCornerRadius, theme.shapes.markerLabelCornerRadius)
    }

    @Test
    fun markerLabelTextDefault_matchesThemedTextStyle() {
        val marker = PersistentMarker(dataIndex = 0)
        val themed = theme.typography.markerLabel.withChartyColor(theme.componentColors.markerLabelText)
        assertEquals(marker.labelTextStyle.withChartyColor(marker.labelTextColor), themed)
    }

    @Test
    fun annotationDefaults_matchLightTheme() {
        val style = AnnotationStyle()
        assertEquals(style.lineColor, theme.componentColors.annotationLine)
        assertEquals(style.labelBackgroundColor, theme.componentColors.annotationLabelBackground)
        assertEquals(style.labelTextColor, theme.componentColors.annotationLabelText)
        assertEquals(style.lineWidth, theme.dimensions.annotationLineWidth)
        assertEquals(style.labelHorizontalPadding, theme.dimensions.annotationLabelHorizontalPadding)
        assertEquals(style.labelVerticalPadding, theme.dimensions.annotationLabelVerticalPadding)
        assertEquals(style.labelCornerRadius, theme.shapes.annotationLabelCornerRadius)
        assertEquals(style.labelTextStyle, theme.typography.annotationLabel)
    }

    @Test
    fun brushSelectionDefaults_matchLightTheme() {
        val style = BrushSelectionStyle()
        assertEquals(style.fill, theme.componentColors.selectionFill)
        assertEquals(style.border, theme.componentColors.selectionBorder)
        assertEquals(style.edgeWidth, theme.dimensions.selectionEdgeWidth)
    }

    @Test
    fun referenceLineDefaults_matchLightTheme() {
        val config = ReferenceLineConfig(value = 1f)
        assertEquals(config.color, theme.componentColors.referenceLine)
        assertEquals(config.strokeWidth, theme.dimensions.referenceLineStrokeWidth)
        assertEquals(config.labelOffset, theme.dimensions.referenceLineLabelOffset)
        val themed = theme.typography.referenceLineLabel.withChartyColor(theme.componentColors.referenceLineLabelText)
        assertEquals(config.labelTextStyle.withChartyColor(config.labelTextColor), themed)
    }

    @Test
    fun referenceBandDefaults_matchLightTheme() {
        val config = ReferenceBandConfig(lowValue = 0f, highValue = 1f)
        assertEquals(config.fill, theme.componentColors.referenceBandFill)
        assertEquals(config.borderColor, theme.componentColors.referenceBandBorder)
        assertEquals(config.fillAlpha, theme.dimensions.referenceBandFillAlpha)
        assertEquals(config.borderWidth, theme.dimensions.referenceBandBorderWidth)
        assertEquals(config.labelPadding, theme.dimensions.referenceBandLabelPadding)
        val themed = theme.typography.referenceBandLabel.withChartyColor(theme.componentColors.referenceBandLabelText)
        assertEquals(config.labelTextStyle.withChartyColor(config.labelTextColor), themed)
    }

    @Test
    fun scrollEdgeFadeDefaults_matchLightTheme() {
        val config = ScrollEdgeFadeConfig()
        assertEquals(config.color, theme.componentColors.scrollEdgeFade)
        assertEquals(config.width, theme.dimensions.scrollEdgeFadeWidth)
        assertEquals(config.alpha, theme.dimensions.scrollEdgeFadeAlpha)
    }
}
