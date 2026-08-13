package com.himanshoe.charty.funnel.internal

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.funnel.config.FunnelOrientation
import com.himanshoe.charty.funnel.data.FunnelStage

private const val HALF = 2f

/**
 * The width of every stage as a fraction of the first stage, which is what makes a funnel a funnel:
 * the opening stage fills the chart and each later stage is drawn in proportion to it.
 *
 * A first stage of zero leaves nothing to be a fraction of, so every stage reports `0f` rather than
 * dividing by zero. A later stage larger than the first is clamped to the full width — a funnel that
 * grows is outside what the shape can express, and stretching the chart to fit it would silently
 * rescale every other stage.
 *
 * @param stages The stages in funnel order.
 * @return One fraction in `0f..1f` per stage, in the same order.
 */
internal fun funnelStageFractions(stages: List<FunnelStage>): List<Float> {
    val first = stages.firstOrNull()?.value ?: 0f
    return stages.fastMap { stage ->
        if (first <= 0f) {
            0f
        } else {
            (stage.value / first).coerceIn(0f, 1f)
        }
    }
}

/**
 * The conversion from the previous stage: the fraction of the stage before that reached this one.
 *
 * @param stages The stages in funnel order.
 * @param index The stage to describe.
 * @return The fraction, or `null` for the first stage and whenever the previous stage was empty.
 */
internal fun funnelConversion(
    stages: List<FunnelStage>,
    index: Int,
): Float? {
    val previous = stages.getOrNull(index - 1)?.value
    val current = stages.getOrNull(index)?.value
    return if (previous == null || current == null || previous <= 0f) {
        null
    } else {
        current / previous
    }
}

/**
 * The slice of the flow axis a stage's band occupies. Bands share the axis evenly, with [gap] pixels
 * of space between neighbours.
 *
 * @param index The stage's index.
 * @param count The number of stages.
 * @param flowStart The flow axis's origin in pixels.
 * @param flowExtent The flow axis's total length in pixels.
 * @param gap Pixels of empty space between consecutive bands.
 * @return The band's bounds along the flow axis; a count of zero yields an empty band at the origin.
 */
internal fun funnelBandBounds(
    index: Int,
    count: Int,
    flowStart: Float,
    flowExtent: Float,
    gap: Float,
): FunnelBand {
    if (count <= 0) {
        return FunnelBand(start = flowStart, end = flowStart)
    }
    val bandExtent = ((flowExtent - gap * (count - 1)) / count).coerceAtLeast(0f)
    val start = flowStart + index * (bandExtent + gap)
    return FunnelBand(start = start, end = start + bandExtent)
}

/**
 * Half the cross-axis extent of a stage's edge: how far the funnel reaches either side of its centre
 * line at that point.
 *
 * @param fraction The stage's width fraction, from [funnelStageFractions].
 * @param crossExtent The cross axis's full length in pixels.
 * @param animationProgress The entry animation's progress from `0f` to `1f`, which the funnel opens
 *   outwards from its centre line with.
 * @return The half extent in pixels.
 */
internal fun funnelHalfExtent(
    fraction: Float,
    crossExtent: Float,
    animationProgress: Float,
): Float = fraction * crossExtent / HALF * animationProgress

/**
 * The bounding rectangle of a stage's band, used for hit testing: the trapezoid's widest edge across
 * the whole band.
 *
 * @param band The stage's slice of the flow axis.
 * @param startHalfExtent Half the cross extent at the band's leading edge.
 * @param endHalfExtent Half the cross extent at the band's trailing edge.
 * @param crossCenter The cross-axis coordinate of the funnel's centre line.
 * @param orientation The direction the funnel runs in.
 * @return The band's bounds in canvas coordinates.
 */
internal fun funnelStageRect(
    band: FunnelBand,
    startHalfExtent: Float,
    endHalfExtent: Float,
    crossCenter: Float,
    orientation: FunnelOrientation,
): Rect {
    val halfExtent = maxOf(startHalfExtent, endHalfExtent)
    return when (orientation) {
        FunnelOrientation.VERTICAL ->
            Rect(
                left = crossCenter - halfExtent,
                top = band.start,
                right = crossCenter + halfExtent,
                bottom = band.end,
            )
        FunnelOrientation.HORIZONTAL ->
            Rect(
                left = band.start,
                top = crossCenter - halfExtent,
                right = band.end,
                bottom = crossCenter + halfExtent,
            )
    }
}
