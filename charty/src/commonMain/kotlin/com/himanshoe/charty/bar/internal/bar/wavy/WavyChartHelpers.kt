package com.himanshoe.charty.bar.internal.bar.wavy

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.gesture.CrosshairManager

/**
 * How many bar-widths the plotting area is divided into per bar: each bar owns one slot of
 * whitespace and one slot of wave, so the wave centres land on the odd multiples of the spacing.
 */
internal const val WAVY_CHART_PHASE_TARGET_MULTIPLIER = 2f

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
    val barSpacing = chartContext.width / (values.size * WAVY_CHART_PHASE_TARGET_MULTIPLIER)
    return List(values.size) { index ->
        Offset(
            x = chartContext.left + barSpacing * (1 + index * 2),
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
