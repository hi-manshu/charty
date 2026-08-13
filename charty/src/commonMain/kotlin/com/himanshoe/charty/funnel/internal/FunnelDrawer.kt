package com.himanshoe.charty.funnel.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.funnel.config.FunnelOrientation

private const val HALF = 2f
private const val LABEL_SPACING = 2f

/**
 * Draws the funnel: one trapezoid per stage, tapering from its own width to the next stage's, with
 * the stage and conversion labels centred on each band.
 *
 * The path is supplied by the caller and reset per stage, so the taper allocates nothing per frame.
 *
 * @param params The frame's drawing inputs.
 */
internal fun DrawScope.drawFunnel(params: FunnelDrawParams) {
    val count = params.stages.size
    if (count == 0) {
        return
    }
    val vertical = params.config.orientation == FunnelOrientation.VERTICAL
    val flowExtent =
        if (vertical) {
            size.height
        } else {
            size.width
        }
    val crossExtent =
        if (vertical) {
            size.width
        } else {
            size.height
        }
    val crossCenter = crossExtent / HALF

    params.stages.fastForEachIndexed { index, stage ->
        val band =
            funnelBandBounds(
                index = index,
                count = count,
                flowStart = 0f,
                flowExtent = flowExtent,
                gap = params.config.stageGap,
            )
        val startHalf =
            funnelHalfExtent(
                fraction = params.fractions[index],
                crossExtent = crossExtent,
                animationProgress = params.animationProgress,
            )
        val endHalf =
            funnelHalfExtent(
                fraction = params.fractions.getOrNull(index + 1) ?: params.fractions[index],
                crossExtent = crossExtent,
                animationProgress = params.animationProgress,
            )
        val rect =
            funnelStageRect(
                band = band,
                startHalfExtent = startHalf,
                endHalfExtent = endHalf,
                crossCenter = crossCenter,
                orientation = params.config.orientation,
            )
        buildTaper(
            params = params,
            band = band,
            startHalf = startHalf,
            endHalf = endHalf,
            crossCenter = crossCenter,
        )
        drawPath(path = params.path, brush = params.brushes[index])
        if (params.recordBounds) {
            params.onStageBoundCalculated(rect to stage)
        }
        drawStageText(
            stageLabel = params.labels.stage.getOrNull(index),
            conversionLabel = params.labels.conversion.getOrNull(index),
            rect = rect,
        )
    }
}

private fun buildTaper(
    params: FunnelDrawParams,
    band: FunnelBand,
    startHalf: Float,
    endHalf: Float,
    crossCenter: Float,
) {
    params.path.reset()
    if (params.config.orientation == FunnelOrientation.VERTICAL) {
        params.path.moveTo(x = crossCenter - startHalf, y = band.start)
        params.path.lineTo(x = crossCenter + startHalf, y = band.start)
        params.path.lineTo(x = crossCenter + endHalf, y = band.end)
        params.path.lineTo(x = crossCenter - endHalf, y = band.end)
    } else {
        params.path.moveTo(x = band.start, y = crossCenter - startHalf)
        params.path.lineTo(x = band.start, y = crossCenter + startHalf)
        params.path.lineTo(x = band.end, y = crossCenter + endHalf)
        params.path.lineTo(x = band.end, y = crossCenter - endHalf)
    }
    params.path.close()
}

private fun DrawScope.drawStageText(
    stageLabel: TextLayoutResult?,
    conversionLabel: TextLayoutResult?,
    rect: Rect,
) {
    val totalHeight =
        (stageLabel?.size?.height ?: 0) + (conversionLabel?.size?.height ?: 0) +
            if (stageLabel != null && conversionLabel != null) {
                LABEL_SPACING
            } else {
                0f
            }
    var y = rect.top + (rect.height - totalHeight) / HALF
    stageLabel?.let { measured ->
        drawCentredText(label = measured, rect = rect, y = y)
        y += measured.size.height + LABEL_SPACING
    }
    conversionLabel?.let { measured -> drawCentredText(label = measured, rect = rect, y = y) }
}

private fun DrawScope.drawCentredText(
    label: TextLayoutResult,
    rect: Rect,
    y: Float,
) {
    if (label.size.width > rect.width || label.size.height > rect.height) {
        return
    }
    drawText(
        textLayoutResult = label,
        topLeft = Offset(x = rect.left + (rect.width - label.size.width) / HALF, y = y),
    )
}
