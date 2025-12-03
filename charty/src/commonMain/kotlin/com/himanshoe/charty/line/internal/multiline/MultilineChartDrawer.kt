package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint

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
        drawLineForSeries(
            pointPositions = pointPositions,
            chartContext = chartContext,
            lineConfig = lineConfig,
            colorList = colorList,
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
 * Draw the line path for a series
 */
private fun DrawScope.drawLineForSeries(
    pointPositions: List<Offset>,
    chartContext: ChartContext,
    lineConfig: LineChartConfig,
    colorList: List<Color>,
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
        brush = Brush.verticalGradient(colorList),
        style = Stroke(
            width = lineConfig.lineWidth,
            cap = lineConfig.strokeCap,
        ),
        alpha = animationProgress,
    )
}

/**
 * Draw points for a series with animation
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
    pointPositions.fastForEachIndexed { index, position ->
        val pointProgress = if (lineConfig.animation is Animation.Enabled) {
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
                    position to MultilinePoint(
                        lineGroup = group,
                        seriesIndex = seriesIndex,
                        dataIndex = index,
                        value = value,
                    ),
                )
            }

            drawCircle(
                brush = Brush.verticalGradient(colorList),
                radius = lineConfig.pointRadius,
                center = position,
                alpha = (pointProgress.coerceIn(0f, 1f) * lineConfig.pointAlpha),
            )
        }
    }
}

