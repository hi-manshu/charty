@file:OptIn(ExperimentalTextApi::class)

package com.himanshoe.charty.common.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

/**
 * Common drawing utilities for charts
 */

/**
 * Draw a reference line if the configuration is not null
 *
 * @param referenceLineConfig The reference line configuration (null to skip drawing)
 * @param chartContext The chart context with dimensions
 * @param orientation The chart orientation
 * @param textMeasurer Text measurer for labels
 */
fun DrawScope.drawReferenceLineIfNeeded(
    referenceLineConfig: ReferenceLineConfig?,
    chartContext: ChartContext,
    orientation: ChartOrientation,
    textMeasurer: TextMeasurer,
) {
    referenceLineConfig?.let { config ->
        drawReferenceLine(
            chartContext = chartContext,
            orientation = orientation,
            config = config,
            textMeasurer = textMeasurer,
        )
    }
}

/**
 * Draw a tooltip if the state is not null
 *
 * @param tooltipState The tooltip state (null to skip drawing)
 * @param tooltipConfig The tooltip configuration
 * @param textMeasurer Text measurer for tooltip text
 * @param chartContext The chart context with dimensions
 */
fun DrawScope.drawTooltipIfNeeded(
    tooltipState: TooltipState?,
    tooltipConfig: TooltipConfig,
    textMeasurer: TextMeasurer,
    chartContext: ChartContext,
) {
    tooltipState?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}

/**
 * Draw a highlighted point (typically for tooltips) with a white outer circle and colored inner circle
 *
 * @param center The center position of the point
 * @param pointRadius The base radius of the point
 * @param colorBrush The brush for the inner circle
 * @param outerRadiusAddition Additional radius for the outer white circle (default 3f)
 * @param innerRadiusAddition Additional radius for the inner colored circle (default 2f)
 */
fun DrawScope.drawHighlightedPoint(
    center: Offset,
    pointRadius: Float,
    colorBrush: Brush,
    outerRadiusAddition: Float = 3f,
    innerRadiusAddition: Float = 2f,
) {
    // Draw outer white circle
    drawCircle(
        color = Color.White,
        radius = pointRadius + outerRadiusAddition,
        center = center,
    )
    // Draw inner colored circle
    drawCircle(
        brush = colorBrush,
        radius = pointRadius + innerRadiusAddition,
        center = center,
    )
}

/**
 * Draw a vertical guideline with customizable appearance
 *
 * @param x The x-coordinate of the line
 * @param chartContext The chart context with dimensions
 * @param color The color of the line (default black with 0.1 alpha)
 * @param strokeWidth The width of the line (default 1.5f)
 * @param alpha The alpha of the line color (default 0.1f)
 */
fun DrawScope.drawVerticalGuideline(
    x: Float,
    chartContext: ChartContext,
    color: Color = Color.Black,
    strokeWidth: Float = 1.5f,
    alpha: Float = 0.1f,
) {
    drawLine(
        color = color.copy(alpha = alpha),
        start = Offset(x, chartContext.top),
        end = Offset(x, chartContext.bottom),
        strokeWidth = strokeWidth,
    )
}

/**
 * Draw a horizontal guideline with customizable appearance
 *
 * @param y The y-coordinate of the line
 * @param chartContext The chart context with dimensions
 * @param color The color of the line (default black with 0.1 alpha)
 * @param strokeWidth The width of the line (default 1.5f)
 * @param alpha The alpha of the line color (default 0.1f)
 */
fun DrawScope.drawHorizontalGuideline(
    y: Float,
    chartContext: ChartContext,
    color: Color = Color.Black,
    strokeWidth: Float = 1.5f,
    alpha: Float = 0.1f,
) {
    drawLine(
        color = color.copy(alpha = alpha),
        start = Offset(chartContext.left, y),
        end = Offset(chartContext.right, y),
        strokeWidth = strokeWidth,
    )
}

/**
 * Draw a circle with an outline (border)
 *
 * @param center The center position of the circle
 * @param radius The radius of the circle
 * @param fillBrush The brush for filling the circle
 * @param outlineColor The color of the outline
 * @param outlineWidth The width of the outline (default 2f)
 */
fun DrawScope.drawCircleWithOutline(
    center: Offset,
    radius: Float,
    fillBrush: Brush,
    outlineColor: Color,
    outlineWidth: Float = 2f,
) {
    // Draw filled circle
    drawCircle(
        brush = fillBrush,
        radius = radius,
        center = center,
    )
    // Draw outline
    drawCircle(
        color = outlineColor,
        radius = radius,
        center = center,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = outlineWidth),
    )
}

