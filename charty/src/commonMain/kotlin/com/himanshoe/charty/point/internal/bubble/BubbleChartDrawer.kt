package com.himanshoe.charty.point.internal.bubble

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.drawReferenceBandIfNeeded
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.point.BubbleBounds
import com.himanshoe.charty.point.BubbleSizeInfo
import com.himanshoe.charty.point.calculateBubbleRadius
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.BubbleData

private const val BUBBLE_HALO_ALPHA = 0.3f
private const val BUBBLE_CORE_RADIUS_FRACTION = 0.85f
private const val BUBBLE_CENTER_DIVISOR = 2f

/**
 * Everything [drawBubbleContent] needs for one canvas pass, bundled so the drawing entry point keeps
 * a single parameter.
 *
 * @property dataList The bubbles being drawn, in the order they appear along the x-axis.
 * @property chartContext The pixel bounds and value range of the plotting area.
 * @property sizeInfo The size extremes the per-bubble radius is interpolated within.
 * @property minBubbleRadius The radius of the smallest bubble, in pixels.
 * @property config The bubble styling, markers, reference band, and reference line to honour.
 * @property color The bubbles' colour, or the gradient they cycle through by index.
 * @property animationProgress The reveal progress, from `0f` (nothing drawn) to `1f` (fully drawn).
 * @property onBubbleClick Invoked when a bubble is tapped; `null` skips collecting [bubbleBounds].
 * @property bubbleBounds Collects every drawn circle paired with its data, for tap hit-testing.
 * @property textMeasurer Measurer used for the reference band, marker, and reference line labels.
 */
internal data class BubbleDrawParams(
    val dataList: List<BubbleData>,
    val chartContext: ChartContext,
    val sizeInfo: BubbleSizeInfo,
    val minBubbleRadius: Float,
    val config: PointChartConfig,
    val color: ChartyColor,
    val animationProgress: Float,
    val onBubbleClick: ((BubbleData) -> Unit)?,
    val bubbleBounds: MutableList<BubbleBounds>,
    val textMeasurer: TextMeasurer,
)

/**
 * Draws one full bubble-chart pass: the optional reference band behind the data, every bubble, the
 * persistent markers, and the optional reference line over it.
 *
 * @param params The data, geometry, styling, and animation state for this pass.
 */
internal fun DrawScope.drawBubbleContent(params: BubbleDrawParams) {
    drawReferenceBandIfNeeded(
        referenceBandConfig = params.config.referenceBand,
        chartContext = params.chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = params.textMeasurer,
    )

    drawAllBubbles(params)

    if (params.config.markers.isNotEmpty()) {
        drawPersistentMarkers(
            chartContext = params.chartContext,
            markers = params.config.markers,
            pointPositions =
                bubbleMarkerPositions(chartContext = params.chartContext, dataList = params.dataList),
            valueLabelFor = { index -> formatMarkerValue(params.dataList[index].yValue) },
            textMeasurer = params.textMeasurer,
        )
    }

    drawReferenceLineIfNeeded(
        referenceLineConfig = params.config.referenceLine,
        chartContext = params.chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = params.textMeasurer,
    )
}

/**
 * Pixel centres of every drawn bubble, ordered the same as [dataList] so a marker index of `-1` lands
 * on the rightmost bubble.
 *
 * @param chartContext The pixel bounds and value range of the plotting area.
 * @param dataList The bubbles being drawn, in the order they appear along the x-axis.
 * @return One position per bubble, at the same indices as [dataList].
 */
internal fun bubbleMarkerPositions(
    chartContext: ChartContext,
    dataList: List<BubbleData>,
): List<Offset> =
    List(dataList.size) { index ->
        Offset(
            x = chartContext.calculateCenteredXPosition(index = index, totalItems = dataList.size),
            y = chartContext.convertValueToYPosition(dataList[index].yValue),
        )
    }

private fun DrawScope.drawAllBubbles(params: BubbleDrawParams) {
    val dataList = params.dataList
    dataList.fastForEachIndexed { index, bubble ->
        val bubbleProgress = index.toFloat() / dataList.size
        val bubbleAnimationProgress = ((params.animationProgress - bubbleProgress) * dataList.size).coerceIn(0f, 1f)

        val bubbleX = params.chartContext.calculateCenteredXPosition(index, dataList.size)
        val bubbleY = params.chartContext.convertValueToYPosition(bubble.yValue)

        val bubbleRadius =
            calculateBubbleRadius(
                bubbleSize = bubble.size,
                minSize = params.sizeInfo.minSize,
                sizeRange = params.sizeInfo.sizeRange,
                minBubbleRadius = params.minBubbleRadius,
                maxBubbleRadius = params.config.pointRadius,
            )

        val bubbleColor =
            when (val color = params.color) {
                is ChartyColor.Solid -> color.color
                is ChartyColor.Gradient -> color.colors[index % color.colors.size]
            }

        if (bubbleAnimationProgress > 0f) {
            val center =
                Offset(
                    x = bubbleX,
                    y = bubbleCenterY(chartContext = params.chartContext, y = bubbleY, radius = bubbleRadius),
                )
            val animatedRadius = bubbleRadius * bubbleAnimationProgress

            if (params.onBubbleClick != null) {
                params.bubbleBounds.add(BubbleBounds(center = center, radius = animatedRadius, data = bubble))
            }

            drawCircle(
                color = bubbleColor.copy(alpha = BUBBLE_HALO_ALPHA),
                radius = animatedRadius,
                center = center,
                alpha = params.config.pointAlpha * bubbleAnimationProgress,
            )
            drawCircle(
                color = bubbleColor,
                radius = (bubbleRadius * BUBBLE_CORE_RADIUS_FRACTION) * bubbleAnimationProgress,
                center = center,
                alpha = params.config.pointAlpha * bubbleAnimationProgress,
            )
        }
    }
}

/**
 * Keeps a bubble inside the plot by insetting its centre from the value position by its own radius.
 *
 * A bubble is sized by its value rather than by the axis, so a point near either end of the range
 * would otherwise spill past the axis line — most visibly at the bottom, where a large bubble on a
 * low value hangs below the plot. When the plot is shorter than the bubble, the centre falls back to
 * the middle so the overflow is at least symmetric.
 */
private fun bubbleCenterY(
    chartContext: ChartContext,
    y: Float,
    radius: Float,
): Float {
    val top = chartContext.top + radius
    val bottom = chartContext.bottom - radius
    return if (top > bottom) {
        (chartContext.top + chartContext.bottom) / BUBBLE_CENTER_DIVISOR
    } else {
        y.coerceIn(minimumValue = top, maximumValue = bottom)
    }
}
