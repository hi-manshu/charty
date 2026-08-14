package com.himanshoe.charty.line.internal.area

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.draw.drawHighlightedPoint
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.drawReferenceBandIfNeeded
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.draw.interpolatedAreaPath
import com.himanshoe.charty.common.draw.interpolatedLinePath
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.ext.createAreaBrush
import com.himanshoe.charty.line.ext.createLineBrush
import com.himanshoe.charty.line.resolveLineInterpolation

private const val HIGHLIGHT_LINE_ALPHA = 0.1f
private const val HIGHLIGHT_LINE_WIDTH = 1.5f
private const val HIGHLIGHT_CIRCLE_OUTER_PADDING = 3f
private const val HIGHLIGHT_CIRCLE_INNER_PADDING = 2f

/**
 * Draws one full area-chart pass: the optional reference band, the filled area and its outline
 * (revealed left-to-right by [AreaChartDrawParams.animationProgress]), the optional points, the
 * persistent markers, and the optional reference line. Does nothing when there are no point
 * positions to connect.
 *
 * @param params The geometry, styling, and animation state for this pass.
 */
internal fun DrawScope.drawAreaChart(params: AreaChartDrawParams) {
    if (params.pointPositions.isEmpty()) {
        return
    }

    drawReferenceBandIfNeeded(
        referenceBandConfig = params.config.referenceBand,
        chartContext = params.chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = params.textMeasurer,
    )

    val axisAnchor = Offset(x = params.chartContext.left, y = params.baselineY)
    val startX = axisAnchor.x
    val endX = params.pointPositions.last().x
    val clipRight = startX + (endX - startX) * params.animationProgress

    val interpolation = resolveLineInterpolation(params.config)
    val areaPath =
        interpolatedAreaPath(
            points = params.pointPositions,
            baselineY = params.baselineY,
            interpolation = interpolation,
            anchor = axisAnchor,
        )
    val areaBrush =
        createAreaBrush(
            color = params.color,
            fillAlpha = params.fillAlpha,
            chartTop = params.chartContext.top,
            chartBottom = params.chartContext.bottom,
        )
    val linePath = interpolatedLinePath(points = params.pointPositions, interpolation = interpolation)
    val lineBrush = createLineBrush(params.color)

    clipRect(right = clipRight) {
        drawPath(path = areaPath, brush = areaBrush, style = Fill)

        drawPath(
            path = linePath,
            brush = lineBrush,
            style = Stroke(width = params.config.lineWidth, cap = params.config.strokeCap),
        )
    }

    if (params.config.showPoints) {
        drawAreaPoints(
            pointPositions = params.pointPositions,
            lineBrush = lineBrush,
            config = params.config,
            animationProgress = params.animationProgress,
        )
    }

    drawPersistentMarkers(
        chartContext = params.chartContext,
        markers = params.config.markers,
        pointPositions = params.pointPositions,
        valueLabelFor = { index -> formatMarkerValue(params.dataList[index].value) },
        textMeasurer = params.textMeasurer,
    )

    drawReferenceLineIfNeeded(
        referenceLineConfig = params.config.referenceLine,
        chartContext = params.chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = params.textMeasurer,
    )
}

/**
 * Draws the point dots on top of the area outline, revealing each one only once the left-to-right
 * reveal has reached it.
 *
 * @param pointPositions The pixel position of every point, in x order.
 * @param lineBrush The brush the outline is stroked with, reused so the dots match the line.
 * @param config Supplies the point radius and opacity.
 * @param animationProgress The reveal progress, from `0f` to `1f`.
 */
internal fun DrawScope.drawAreaPoints(
    pointPositions: List<Offset>,
    lineBrush: Brush,
    config: LineChartConfig,
    animationProgress: Float,
) {
    pointPositions.fastForEachIndexed { index, position ->
        val pointProgress = index.toFloat() / (pointPositions.size - 1).coerceAtLeast(1)
        if (pointProgress <= animationProgress) {
            drawCircle(
                brush = lineBrush,
                radius = config.pointRadius,
                center = position,
                alpha = config.pointAlpha,
            )
        }
    }
}

/**
 * Highlights the point a tooltip is currently anchored to with a full-height guide line and a
 * ringed dot. Does nothing when no tooltip is showing or the tooltip's content matches no point.
 *
 * @param tooltipState The active tooltip, or `null` when none is showing.
 * @param lineConfig Supplies the point radius and the formatter used to match the tooltip content.
 * @param pointBounds Every point position paired with its data.
 * @param chartContext The pixel bounds of the plotting area, giving the guide line its extent.
 * @param color The colour or gradient the inner dot is filled with.
 */
internal fun DrawScope.drawAreaTooltipHighlight(
    tooltipState: TooltipState?,
    lineConfig: LineChartConfig,
    pointBounds: List<Pair<Offset, LineData>>,
    chartContext: ChartContext,
    color: ChartyColor,
) {
    tooltipState?.let { state ->
        val clickedPosition =
            pointBounds
                .fastFirstOrNull { (_, data) ->
                    lineConfig.tooltipFormatter(data) == state.content
                }?.first

        clickedPosition?.let { position ->
            drawLine(
                color = Color.Black.copy(alpha = HIGHLIGHT_LINE_ALPHA),
                start = Offset(position.x, chartContext.top),
                end = Offset(position.x, chartContext.bottom),
                strokeWidth = HIGHLIGHT_LINE_WIDTH,
            )
            drawHighlightedPoint(
                center = position,
                pointRadius = lineConfig.pointRadius,
                colorBrush = Brush.linearGradient(color.value),
                outerRadiusAddition = HIGHLIGHT_CIRCLE_OUTER_PADDING,
                innerRadiusAddition = HIGHLIGHT_CIRCLE_INNER_PADDING,
            )
        }
    }
}
