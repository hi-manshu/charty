package com.himanshoe.charty.bar.internal.bar.wavy

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.baselineY
import com.himanshoe.charty.common.gesture.CrosshairManager
import kotlin.math.PI

/** One full turn of the sine, which is where the wave's phase animation ends up back where it began. */
internal const val FULL_WAVE_CYCLE_RADIANS = (2 * PI).toFloat()

/**
 * Half the horizontal slot one bar owns, in pixels.
 *
 * The plotting area is divided into two slots per bar — one of whitespace, one of wave — so the wave
 * centres land on the odd multiples of this and each wave has clear air either side. Three call sites
 * derived it separately from the same two inputs.
 */
internal fun waveSlotHalfWidth(
    chartContext: ChartContext,
    barCount: Int,
): Float = chartContext.width / (barCount * 2f)

/** The floor on wave segments, below which the sine curve stops reading as a curve. */
internal const val MIN_WAVE_SEGMENTS = 4

/** The floor on the configured bar-width fraction, so a wave always has room to swing. */
internal const val MIN_BAR_WIDTH_FRACTION = 0.1f

/**
 * The anchor point of every wave: horizontally at the centre of its bar slot, vertically at the
 * value it represents. Markers and the crosshair both snap to these.
 *
 * @param chartContext The pixel bounds and value range of the plotting area.
 * @param values The bar values, in the order they are drawn.
 * @return One anchor per value, at the same indices.
 */
internal fun wavyPointPositions(
    chartContext: ChartContext,
    values: List<Float>,
): List<Offset> {
    val slotHalfWidth = waveSlotHalfWidth(chartContext = chartContext, barCount = values.size)
    return List(values.size) { index ->
        Offset(
            x = waveSlotCenterX(chartContext = chartContext, slotHalfWidth = slotHalfWidth, index = index),
            y = chartContext.convertValueToYPosition(values[index]),
        )
    }
}

/**
 * Refills [crosshairBounds] with the current frame's wave anchors so the crosshair snaps to where
 * the bars actually are. Clears the bounds and stops there when the crosshair is off or there is
 * nothing to draw.
 *
 * @param chartContext The pixel bounds and value range of the plotting area.
 * @param dataList The bars currently drawn.
 * @param crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @param crosshairBounds The bounds list to refill, owned by the chart across frames.
 */
internal fun populateWavyCrosshairBounds(
    chartContext: ChartContext,
    dataList: List<BarData>,
    crosshairManager: CrosshairManager<BarData>?,
    crosshairBounds: MutableList<Pair<Offset, BarData>>,
) {
    crosshairBounds.clear()
    if (crosshairManager == null) {
        return
    }
    val barCount = dataList.size
    if (barCount == 0) {
        return
    }
    val positions = wavyPointPositions(chartContext = chartContext, values = dataList.fastMap { it.value })
    dataList.fastForEachIndexed { index, barData ->
        crosshairBounds.add(positions[index] to barData)
    }
}

/**
 * The tap hit area of every wave: the wave is a thin stroked sine curve that is all but impossible
 * to hit directly, so a tap is resolved against the whole column the wave occupies — its full slot
 * width horizontally, and the span between its value and the baseline vertically, grown by the
 * stroke so a flat wave still has a target.
 *
 * @param chartContext The pixel bounds and value range of the plotting area.
 * @param values The bar values, in the order they are drawn.
 * @param minValue The lowest value on the axis, which decides where the baseline sits.
 * @param strokeWidthPx The wave stroke width, in pixels.
 * @return One rect per value, at the same indices.
 */
internal fun wavyBarHitRects(
    chartContext: ChartContext,
    values: List<Float>,
    minValue: Float,
    strokeWidthPx: Float,
): List<Rect> {
    val baselineY = chartContext.baselineY(minValue = minValue, drawNegativesInPlace = true)
    val slotHalfWidth = waveSlotHalfWidth(chartContext = chartContext, barCount = values.size)
    val halfStroke = strokeWidthPx / 2f
    return List(values.size) { index ->
        val centerX = waveSlotCenterX(chartContext = chartContext, slotHalfWidth = slotHalfWidth, index = index)
        val valueY = chartContext.convertValueToYPosition(values[index])
        Rect(
            left = centerX - slotHalfWidth,
            top = minOf(valueY, baselineY) - halfStroke,
            right = centerX + slotHalfWidth,
            bottom = maxOf(valueY, baselineY) + halfStroke,
        )
    }
}

/**
 * Refills [barBounds] with the current frame's wave hit areas so a tap resolves against where the
 * waves actually are. Clears the bounds and stops there when [enabled] is `false`.
 *
 * @param chartContext The pixel bounds and value range of the plotting area.
 * @param dataList The bars currently drawn.
 * @param enabled Whether anything hit-tests the bounds this frame.
 * @param minValue The lowest value on the axis, which decides where the baseline sits.
 * @param strokeWidthPx The wave stroke width, in pixels.
 * @param barBounds The bounds list to refill, owned by the chart across frames.
 */
internal fun populateWavyBarBounds(
    chartContext: ChartContext,
    dataList: List<BarData>,
    enabled: Boolean,
    minValue: Float,
    strokeWidthPx: Float,
    barBounds: MutableList<Pair<Rect, BarData>>,
) {
    barBounds.clear()
    if (!enabled || dataList.isEmpty()) {
        return
    }
    val rects =
        wavyBarHitRects(
            chartContext = chartContext,
            values = dataList.fastMap { it.value },
            minValue = minValue,
            strokeWidthPx = strokeWidthPx,
        )
    dataList.fastForEachIndexed { index, barData -> barBounds.add(rects[index] to barData) }
}

/**
 * The horizontal centre of the slot at [index].
 *
 * The formula reads oddly because [slotHalfWidth] is half a slot, not a gap: slot `i` runs from
 * `2i` to `2i + 2` half-widths, so its centre is at `2i + 1`. Written out at three call sites it
 * looked like three different pieces of arithmetic rather than one idea.
 */
internal fun waveSlotCenterX(
    chartContext: ChartContext,
    slotHalfWidth: Float,
    index: Int,
): Float = chartContext.left + slotHalfWidth * (1 + index * 2)
