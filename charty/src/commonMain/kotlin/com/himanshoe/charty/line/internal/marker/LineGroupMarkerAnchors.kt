package com.himanshoe.charty.line.internal.marker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.line.data.LineGroup

/**
 * The value a persistent marker anchors to at each x position of a multiline chart: the largest
 * value across that group's series, which is the visually topmost point of the cluster. A group with
 * no series anchors at zero.
 *
 * Anchoring on the top of the cluster keeps a marker's dot and callout clear of the lines below it,
 * and matches how a reader points at "that spike" rather than at one particular series.
 *
 * @receiver The groups being drawn, one x position per group and one value per series.
 * @return One anchor value per group, at the same indices as the receiver.
 */
internal fun List<LineGroup>.topSeriesValues(): List<Float> = fastMap { group -> group.values.maxOrNull() ?: 0f }

/**
 * The value a persistent marker anchors to at each x position of a stacked area chart: the group's
 * full total, which is the top of the stack there. A group with no series anchors at zero.
 *
 * @receiver The groups being drawn, one x position per group and one value per series.
 * @return One anchor value per group, at the same indices as the receiver.
 */
internal fun List<LineGroup>.stackedTotalValues(): List<Float> = fastMap { group -> group.values.sum() }

/**
 * Maps anchor values onto the canvas, one per x position, so a marker's `dataIndex` of `-1` lands on
 * the rightmost group.
 *
 * @param values The anchor value of every group, in the order they appear along the x-axis.
 * @return The pixel position of every anchor, at the same indices as [values].
 */
internal fun ChartContext.markerAnchorPositions(values: List<Float>): List<Offset> =
    values.fastMapIndexed { index, value ->
        Offset(
            x = calculateCenteredXPosition(index = index, totalItems = values.size),
            y = convertValueToYPosition(value),
        )
    }
