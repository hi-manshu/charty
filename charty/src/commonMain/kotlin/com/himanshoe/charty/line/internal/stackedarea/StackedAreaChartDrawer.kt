package com.himanshoe.charty.line.internal.stackedarea

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.draw.drawReferenceBand
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.StackedAreaPoint
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import com.himanshoe.charty.line.internal.path.interpolatedAreaPath
import com.himanshoe.charty.line.internal.path.interpolatedLinePath
import com.himanshoe.charty.line.resolveLineInterpolation

/**
 * Draw a single stacked area series
 */
internal fun DrawScope.drawStackedAreaSeries(params: StackedAreaSeriesParams) {
    if (params.cumulativePositions.isEmpty()) {
        return
    }
    val interpolation = resolveLineInterpolation(params.lineConfig)
    val anchor = Offset(x = params.startX, y = params.baselineY)
    val areaPath =
        interpolatedAreaPath(
            points = params.cumulativePositions,
            baselineY = params.baselineY,
            interpolation = interpolation,
            anchor = anchor,
        )

    drawPath(
        path = areaPath,
        color = params.seriesColor.copy(alpha = params.fillAlpha),
        style = Fill,
        alpha = params.animationProgress,
    )
    val linePath =
        interpolatedLinePath(
            points = params.cumulativePositions,
            interpolation = interpolation,
            anchor = anchor,
        )

    drawPath(
        path = linePath,
        color = params.seriesColor,
        style =
            Stroke(
                width = params.lineConfig.lineWidth,
                cap = params.lineConfig.strokeCap,
            ),
        alpha = params.animationProgress,
    )
    if (params.onSegmentBoundsCalculated != null) {
        addSegmentBounds(
            dataList = params.dataList,
            seriesIndex = params.seriesIndex,
            cumulativePositions = params.cumulativePositions,
            lowerPositions = params.lowerPositions,
            onSegmentBoundsCalculated = params.onSegmentBoundsCalculated,
        )
    }
}

/**
 * Add segment bounds for click detection
 */
private fun addSegmentBounds(
    dataList: List<LineGroup>,
    seriesIndex: Int,
    cumulativePositions: List<Offset>,
    lowerPositions: List<Offset>,
    onSegmentBoundsCalculated: (Triple<Rect, Path, StackedAreaPoint>) -> Unit,
) {
    dataList.fastForEachIndexed { dataIndex, group ->
        val segmentValue = group.values.getOrNull(seriesIndex) ?: 0f
        val upperPoint = cumulativePositions.getOrNull(dataIndex)
        val lowerPoint = lowerPositions.getOrNull(dataIndex)

        if (upperPoint != null && lowerPoint != null && dataIndex < cumulativePositions.size - 1) {
            val nextUpperPoint = cumulativePositions[dataIndex + 1]
            val nextLowerPoint = lowerPositions[dataIndex + 1]

            val segmentPath =
                Path().apply {
                    moveTo(upperPoint.x, upperPoint.y)
                    lineTo(nextUpperPoint.x, nextUpperPoint.y)
                    lineTo(nextLowerPoint.x, nextLowerPoint.y)
                    lineTo(lowerPoint.x, lowerPoint.y)
                    close()
                }

            val minX = minOf(upperPoint.x, lowerPoint.x, nextUpperPoint.x, nextLowerPoint.x)
            val maxX = maxOf(upperPoint.x, lowerPoint.x, nextUpperPoint.x, nextLowerPoint.x)
            val minY = minOf(upperPoint.y, lowerPoint.y, nextUpperPoint.y, nextLowerPoint.y)
            val maxY = maxOf(upperPoint.y, lowerPoint.y, nextUpperPoint.y, nextLowerPoint.y)

            val cumulativeValue = group.calculateCumulativeValue(seriesIndex)

            onSegmentBoundsCalculated(
                Triple(
                    Rect(left = minX, top = minY, right = maxX, bottom = maxY),
                    segmentPath,
                    StackedAreaPoint(
                        lineGroup = group,
                        seriesIndex = seriesIndex,
                        dataIndex = dataIndex,
                        value = segmentValue,
                        cumulativeValue = cumulativeValue,
                    ),
                ),
            )
        }
    }
}

/**
 * Draws one full stacked area pass: the optional reference band, the crosshair snap points, every
 * band from the top of the stack down so lower bands overlap correctly, the canvas tooltip when no
 * crosshair is active, the interaction overlays, and finally the crosshair itself.
 *
 * @param params The data, geometry, styling, and interaction state for this pass.
 */
internal fun DrawScope.drawStackedAreaContent(params: StackedAreaDrawParams) {
    val dataList = params.dataList
    val chartContext = params.chartContext
    val colorList = params.colorList
    val lineConfig = params.lineConfig
    val fillAlpha = params.fillAlpha
    val animationProgress = params.animationProgress
    val onAreaClick = params.onAreaClick
    val areaSegmentBounds = params.areaSegmentBounds
    val tooltipState = params.tooltipState
    val textMeasurer = params.textMeasurer
    val interactionConfig = params.interactionConfig
    val baselineY = chartContext.bottom
    val startX = chartContext.left
    val seriesCount = dataList.getOrNull(0)?.values?.size ?: 0

    lineConfig.referenceBand?.let { band ->
        drawReferenceBand(
            chartContext = chartContext,
            orientation = ChartOrientation.VERTICAL,
            config = band,
            textMeasurer = textMeasurer,
        )
    }

    params.crosshairBounds?.let { bounds ->
        bounds.clear()
        if (seriesCount > 0) {
            chartContext.calculateCumulativePositions(dataList, seriesCount - 1).fastForEachIndexed { idx, pos ->
                dataList.getOrNull(idx)?.let { bounds.add(pos to it) }
            }
        }
    }

    for (seriesIndex in seriesCount - 1 downTo 0) {
        val seriesColor = colorList[seriesIndex % colorList.size]
        val cumulativePositions = chartContext.calculateCumulativePositions(dataList, seriesIndex)
        val lowerPositions = chartContext.calculateLowerPositions(dataList, seriesIndex, baselineY)

        drawStackedAreaSeries(
            StackedAreaSeriesParams(
                seriesIndex = seriesIndex,
                seriesColor = seriesColor,
                cumulativePositions = cumulativePositions,
                lowerPositions = lowerPositions,
                startX = startX,
                baselineY = baselineY,
                lineConfig = lineConfig,
                fillAlpha = fillAlpha,
                animationProgress = animationProgress,
                dataList = dataList,
                onSegmentBoundsCalculated =
                    if (onAreaClick != null) {
                        { bounds -> areaSegmentBounds.add(bounds) }
                    } else {
                        null
                    },
            ),
        )
    }

    if (params.crosshairManager == null) {
        tooltipState?.let { state ->
            drawTooltip(
                tooltipState = state,
                config = lineConfig.tooltipConfig,
                textMeasurer = textMeasurer,
                chartWidth = chartContext.right,
                chartTop = chartContext.top,
                chartBottom = chartContext.bottom,
            )
        }
    }
    drawInteractionOverlays(
        interactionConfig = interactionConfig,
        chartContext = chartContext,
        totalItems = dataList.size,
        textMeasurer = textMeasurer,
    )

    params.crosshairState?.let { state ->
        lineConfig.crosshairConfig?.let { config ->
            val dotColor = ChartyColor.Solid(colorList.firstOrNull() ?: Color.Transparent)
            drawLineChartCrosshair(
                state = state,
                config = config,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
                chartColor = dotColor,
                drawLabel = params.drawCrosshairLabel,
            )
        }
    }
}
