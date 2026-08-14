package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.animation.isAnimated
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.drawReferenceBand
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.draw.interpolatedAreaPath
import com.himanshoe.charty.common.draw.interpolatedLinePath
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint
import com.himanshoe.charty.line.ext.createAreaBrush
import com.himanshoe.charty.line.ext.createLineBrush
import com.himanshoe.charty.line.internal.marker.markerAnchorPositions
import com.himanshoe.charty.line.internal.marker.topSeriesValues
import com.himanshoe.charty.line.resolveLineInterpolation

/**
 * Draw a single line series on the chart
 */
internal fun DrawScope.drawLineSeries(
    seriesIndex: Int,
    dataList: List<LineGroup>,
    chartContext: ChartContext,
    lineConfig: LineChartConfig,
    colorList: List<Color>,
    animationProgress: Float,
    pointBounds: MutableList<Pair<Offset, MultilinePoint>>?,
) {
    val pointPositions = chartContext.calculateSeriesPointPositions(dataList, seriesIndex)

    if (pointPositions.isNotEmpty()) {
        val seriesColor = colorList[seriesIndex % colorList.size]
        if (lineConfig.showGradientFill) {
            val fillPath =
                interpolatedAreaPath(
                    points = pointPositions,
                    baselineY = chartContext.bottom,
                    interpolation = resolveLineInterpolation(lineConfig),
                )
            val fillBrush =
                createAreaBrush(
                    color = ChartyColor.Solid(seriesColor),
                    fillAlpha = lineConfig.gradientFillAlpha,
                    chartTop = chartContext.top,
                    chartBottom = chartContext.bottom,
                )
            drawPath(path = fillPath, brush = fillBrush, style = Fill, alpha = animationProgress)
        }
        drawLineForSeries(
            pointPositions = pointPositions,
            seriesColor = seriesColor,
            chartContext = chartContext,
            lineConfig = lineConfig,
            animationProgress = animationProgress,
        )
    }

    if (lineConfig.showPoints) {
        drawPointsForSeries(
            pointPositions = pointPositions,
            seriesIndex = seriesIndex,
            dataList = dataList,
            lineConfig = lineConfig,
            colorList = colorList,
            animationProgress = animationProgress,
            pointBounds = pointBounds,
        )
    }
}

/**
 * Draw the line path for a single series using its resolved series color.
 */
private fun DrawScope.drawLineForSeries(
    pointPositions: List<Offset>,
    seriesColor: Color,
    chartContext: ChartContext,
    lineConfig: LineChartConfig,
    animationProgress: Float,
) {
    val path =
        interpolatedLinePath(
            points = pointPositions,
            interpolation = resolveLineInterpolation(lineConfig),
            anchor = Offset(x = chartContext.left, y = chartContext.bottom),
        )

    drawPath(
        path = path,
        brush = createLineBrush(ChartyColor.Solid(seriesColor)),
        style =
            Stroke(
                width = lineConfig.lineWidth,
                cap = lineConfig.strokeCap,
            ),
        alpha = animationProgress,
    )
}

/**
 * Draw points for a series with animation, using the series' own color.
 */
private fun DrawScope.drawPointsForSeries(
    pointPositions: List<Offset>,
    seriesIndex: Int,
    dataList: List<LineGroup>,
    lineConfig: LineChartConfig,
    colorList: List<Color>,
    animationProgress: Float,
    pointBounds: MutableList<Pair<Offset, MultilinePoint>>?,
) {
    val seriesColor = colorList[seriesIndex % colorList.size]
    pointPositions.fastForEachIndexed { index, position ->
        // Dots appear left to right rather than all at once: a dot's own progress is how far along
        // the series it sits, held back by how far the line has actually been drawn. The first dot is
        // 1/n rather than 0 so that it is visible from the opening frame instead of after one step.
        val pointProgress =
            if (lineConfig.animation.isAnimated) {
                ((index + 1).toFloat() / pointPositions.size)
                    .coerceAtMost(animationProgress * MultilineChartConstants.POINT_REVEAL_LEAD)
            } else {
                1f
            }

        if (pointProgress > 0f) {
            if (pointBounds != null) {
                val group = dataList[index]
                val value = group.values.getOrNull(seriesIndex) ?: 0f
                pointBounds.add(
                    position to
                        MultilinePoint(
                            lineGroup = group,
                            seriesIndex = seriesIndex,
                            dataIndex = index,
                            value = value,
                        ),
                )
            }

            drawCircle(
                color = seriesColor,
                radius = lineConfig.pointRadius,
                center = position,
                alpha = (pointProgress.coerceIn(0f, 1f) * lineConfig.pointAlpha),
            )
        }
    }
}

/**
 * Draws the persistent markers on top of every series, anchored on the topmost series at each x
 * position (see [topSeriesValues]). A marker without its own label shows that anchor's value.
 *
 * @param p The data, geometry, and styling for this pass.
 */
private fun DrawScope.drawMultilineMarkers(p: MultilineDrawParams) {
    if (p.lineConfig.markers.isEmpty()) {
        return
    }
    val anchorValues = p.dataList.topSeriesValues()
    drawPersistentMarkers(
        chartContext = p.chartContext,
        markers = p.lineConfig.markers,
        pointPositions = p.chartContext.markerAnchorPositions(anchorValues),
        valueLabelFor = { index -> formatMarkerValue(anchorValues[index]) },
        textMeasurer = p.textMeasurer,
    )
}

/**
 * Draws one full multiline pass: the optional reference band, the crosshair snap points, every
 * series back to front, the persistent markers, the optional reference line, the canvas tooltip when
 * no crosshair is active, the interaction overlays, and finally the crosshair itself.
 *
 * @param p The data, geometry, styling, and interaction state for this pass.
 */
internal fun DrawScope.drawMultilineContent(p: MultilineDrawParams) {
    p.pointBounds.clear()
    p.crosshairBounds.clear()
    p.lineConfig.referenceBand?.let { band ->
        drawReferenceBand(
            chartContext = p.chartContext,
            orientation = ChartOrientation.VERTICAL,
            config = band,
            textMeasurer = p.textMeasurer,
        )
    }
    val seriesCount =
        p.dataList
            .getOrNull(0)
            ?.values
            ?.size ?: 0

    if (p.crosshairManager != null) {
        p.chartContext.calculateSeriesPointPositions(p.dataList, 0).fastForEachIndexed { index, pos ->
            val group = p.dataList.getOrNull(index) ?: return@fastForEachIndexed
            p.crosshairBounds.add(
                pos to
                    MultilinePoint(
                        lineGroup = group,
                        seriesIndex = 0,
                        dataIndex = index,
                        value =
                            group.values.getOrNull(0) ?: 0f,
                    ),
            )
        }
    }

    for (seriesIndex in 0 until seriesCount) {
        drawLineSeries(
            seriesIndex = seriesIndex,
            dataList = p.dataList,
            chartContext = p.chartContext,
            lineConfig = p.lineConfig,
            colorList = p.colorList,
            animationProgress = p.animationProgress,
            pointBounds =
                p.pointBounds.takeIf { p.recordPointBounds },
        )
    }

    drawMultilineMarkers(p)

    drawReferenceLineIfNeeded(
        referenceLineConfig = p.lineConfig.referenceLine,
        chartContext = p.chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = p.textMeasurer,
    )

    p.tooltipState?.takeIf { p.drawTooltipBubble }?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = p.tooltipConfig,
            textMeasurer = p.textMeasurer,
            chartWidth = p.chartContext.right,
            chartTop = p.chartContext.top,
            chartBottom = p.chartContext.bottom,
        )
    }
    drawInteractionOverlays(
        interactionConfig = p.interactionConfig,
        chartContext = p.chartContext,
        totalItems = p.dataList.size,
        textMeasurer = p.textMeasurer,
    )

    p.crosshairState?.let { state ->
        p.lineConfig.crosshairConfig?.let { config ->
            drawMultilineChartCrosshair(
                state = state,
                config = config,
                chartContext = p.chartContext,
                dataList = p.dataList,
                colorList = p.colorList,
                textMeasurer = p.textMeasurer,
            )
        }
    }
}
