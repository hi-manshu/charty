package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.animation.isAnimated
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint
import com.himanshoe.charty.line.ext.createAreaBrush
import com.himanshoe.charty.line.ext.createAreaPath
import com.himanshoe.charty.line.ext.createLineBrush

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
            val fillPath = createAreaPath(pointPositions, chartContext.bottom, lineConfig.smoothCurve)
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
    val path = Path()
    val startX = chartContext.left
    val startY = chartContext.bottom

    if (lineConfig.smoothCurve) {
        path.drawSmoothMultiline(pointPositions, startX, startY)
    } else {
        path.drawStraightMultiline(pointPositions, startX, startY)
    }

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
        val pointProgress =
            if (lineConfig.animation.isAnimated) {
                ((index + MultilineChartConstants.SERIES_INDEX_OFFSET).toFloat() / pointPositions.size)
                    .coerceAtMost(animationProgress * MultilineChartConstants.ANIMATION_PROGRESS_MULTIPLIER)
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
