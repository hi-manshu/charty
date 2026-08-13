package com.himanshoe.charty.common.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.toBrush
import com.himanshoe.charty.color.toHorizontalBrush
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.config.ReferenceBandConfig
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

/**
 * Common drawing utilities for charts
 */

/**
 * Draws a reference line on the chart if a [ReferenceLineConfig] is provided.
 *
 * @param referenceLineConfig The configuration for the reference line. If `null`, no line is drawn.
 * @param chartContext The context of the chart, providing dimensions and value range.
 * @param orientation The orientation of the chart, either [ChartOrientation.VERTICAL] or [ChartOrientation.HORIZONTAL].
 * @param textMeasurer A [TextMeasurer] used for measuring the label text.
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
 * Draws a reference band on the chart if a [ReferenceBandConfig] is provided. Call before the data
 * so the band renders behind it.
 *
 * @param referenceBandConfig The band configuration. If `null`, nothing is drawn.
 * @param chartContext The context of the chart, providing dimensions and value range.
 * @param orientation The orientation of the value axis.
 * @param textMeasurer A [TextMeasurer] used for the optional label.
 */
fun DrawScope.drawReferenceBandIfNeeded(
    referenceBandConfig: ReferenceBandConfig?,
    chartContext: ChartContext,
    orientation: ChartOrientation,
    textMeasurer: TextMeasurer,
) {
    referenceBandConfig?.let { config ->
        drawReferenceBand(
            chartContext = chartContext,
            orientation = orientation,
            config = config,
            textMeasurer = textMeasurer,
        )
    }
}

/**
 * Draws a tooltip on the chart if a [TooltipState] is provided.
 *
 * @param tooltipState The state of the tooltip. If `null`, no tooltip is drawn.
 * @param tooltipConfig The configuration for the tooltip's appearance. Charts resolve their own
 *   `null` config against the ambient theme before drawing, so a `null` here means the caller asked
 *   for no tooltip at all and nothing is drawn.
 * @param textMeasurer A [TextMeasurer] used for measuring the tooltip text.
 * @param chartContext The context of the chart, providing dimensions.
 */
fun DrawScope.drawTooltipIfNeeded(
    tooltipState: TooltipState?,
    tooltipConfig: TooltipConfig?,
    textMeasurer: TextMeasurer,
    chartContext: ChartContext,
) {
    val state = tooltipState ?: return
    tooltipConfig?.let { config ->
        drawTooltip(
            tooltipState = state,
            config = config,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}

/**
 * Draws a highlighted point, typically used for tooltips, with a white outer circle and a colored inner circle.
 *
 * @param center The center position of the point.
 * @param pointRadius The base radius of the point.
 * @param colorBrush The [Brush] used for the inner circle.
 * @param outerRadiusAddition The additional radius for the outer halo circle.
 * @param innerRadiusAddition The additional radius for the inner colored circle.
 * @param haloColor The color or gradient of the outer halo that separates the point from the data.
 */
fun DrawScope.drawHighlightedPoint(
    center: Offset,
    pointRadius: Float,
    colorBrush: Brush,
    outerRadiusAddition: Float = 3f,
    innerRadiusAddition: Float = 2f,
    haloColor: ChartyColor = ChartyColor.Solid(Color.White),
) {
    drawCircle(
        brush = haloColor.toBrush(),
        radius = pointRadius + outerRadiusAddition,
        center = center,
    )
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
 * @param color The color or gradient of the line (default black)
 * @param strokeWidth The width of the line (default 1.5f)
 * @param alpha The opacity applied to the line color (default 0.1f)
 */
fun DrawScope.drawVerticalGuideline(
    x: Float,
    chartContext: ChartContext,
    color: ChartyColor = ChartyColor.Solid(Color.Black),
    strokeWidth: Float = 1.5f,
    alpha: Float = 0.1f,
) {
    drawLine(
        brush = color.toBrush(),
        alpha = alpha,
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
 * @param color The color or gradient of the line (default black)
 * @param strokeWidth The width of the line (default 1.5f)
 * @param alpha The opacity applied to the line color (default 0.1f)
 */
fun DrawScope.drawHorizontalGuideline(
    y: Float,
    chartContext: ChartContext,
    color: ChartyColor = ChartyColor.Solid(Color.Black),
    strokeWidth: Float = 1.5f,
    alpha: Float = 0.1f,
) {
    drawLine(
        brush = color.toHorizontalBrush(),
        alpha = alpha,
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
 * @param outlineColor The color or gradient of the outline
 * @param outlineWidth The width of the outline (default 2f)
 */
fun DrawScope.drawCircleWithOutline(
    center: Offset,
    radius: Float,
    fillBrush: Brush,
    outlineColor: ChartyColor,
    outlineWidth: Float = 2f,
) {
    drawCircle(
        brush = fillBrush,
        radius = radius,
        center = center,
    )
    drawCircle(
        brush = outlineColor.toBrush(),
        radius = radius,
        center = center,
        style =
            androidx.compose.ui.graphics.drawscope
                .Stroke(width = outlineWidth),
    )
}
