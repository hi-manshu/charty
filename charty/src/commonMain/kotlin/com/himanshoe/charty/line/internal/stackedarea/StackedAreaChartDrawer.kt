package com.himanshoe.charty.line.internal.stackedarea

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.StackedAreaPoint
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
