package com.himanshoe.charty.common.annotation

import androidx.compose.ui.geometry.Offset

private const val CATEGORY_CENTER_OFFSET = 0.5f
private const val HALF = 2f

/**
 * Resolves an [AnnotationAnchor] to a pixel position inside the plot rectangle. Kept free of
 * `ChartContext` and of any Compose runtime state so the mapping can be exercised directly.
 *
 * The result is always clamped into `[left, right] x [top, bottom]`, so an anchor that points off
 * the visible window still yields a drawable position on the plot edge.
 *
 * @param anchor The anchor to resolve.
 * @param left The x-coordinate of the plot's left edge.
 * @param top The y-coordinate of the plot's top edge.
 * @param right The x-coordinate of the plot's right edge.
 * @param bottom The y-coordinate of the plot's bottom edge.
 * @param minValue The value mapped to the plot's bottom edge.
 * @param maxValue The value mapped to the plot's top edge.
 * @param totalItems The number of categories currently laid out across the plot.
 * @param centeredXOf Supplies the centre x of a category index, letting a streaming or windowed
 *   layout override the default even division of the plot width.
 * @return The clamped pixel position the annotation attaches to.
 */
internal fun resolveAnnotationAnchor(
    anchor: AnnotationAnchor,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    minValue: Float,
    maxValue: Float,
    totalItems: Int,
    centeredXOf: ((Int) -> Float)? = null,
): Offset {
    val plotCenterX = (left + right) / HALF
    val position =
        when (anchor) {
            is AnnotationAnchor.DataPoint -> {
                val indexX = centeredXOf?.invoke(anchor.index)
                val fallbackX =
                    categoryCenterX(
                        position = anchor.index.toFloat(),
                        left = left,
                        right = right,
                        totalItems = totalItems,
                    )
                Offset(x = indexX ?: fallbackX, y = (top + bottom) / HALF)
            }

            is AnnotationAnchor.Value -> {
                val anchorX = anchor.x
                val valueX =
                    if (anchorX == null) {
                        plotCenterX
                    } else {
                        categoryCenterX(position = anchorX, left = left, right = right, totalItems = totalItems)
                    }
                val valueY =
                    valueToY(value = anchor.y, top = top, bottom = bottom, minValue = minValue, maxValue = maxValue)
                Offset(x = valueX, y = valueY)
            }

            is AnnotationAnchor.PixelOffset -> Offset(x = left + anchor.x, y = top + anchor.y)
        }
    return Offset(x = position.x.coerceIn(left, right), y = position.y.coerceIn(top, bottom))
}

/**
 * Maps a continuous category [position] (`0f` is the first category) to the x-coordinate of that
 * category's centre, falling back to the plot's horizontal centre when there are no categories.
 *
 * @param position The continuous category position.
 * @param left The x-coordinate of the plot's left edge.
 * @param right The x-coordinate of the plot's right edge.
 * @param totalItems The number of categories currently laid out across the plot.
 * @return The unclamped centre x-coordinate.
 */
internal fun categoryCenterX(
    position: Float,
    left: Float,
    right: Float,
    totalItems: Int,
): Float =
    if (totalItems <= 0) {
        (left + right) / HALF
    } else {
        left + (right - left) * (position + CATEGORY_CENTER_OFFSET) / totalItems
    }

/**
 * Maps a data [value] to its y-coordinate, mirroring the chart's own value scale.
 *
 * @param value The data value to map.
 * @param top The y-coordinate of the plot's top edge.
 * @param bottom The y-coordinate of the plot's bottom edge.
 * @param minValue The value mapped to [bottom].
 * @param maxValue The value mapped to [top].
 * @return The unclamped y-coordinate, or [bottom] when the range is empty.
 */
internal fun valueToY(
    value: Float,
    top: Float,
    bottom: Float,
    minValue: Float,
    maxValue: Float,
): Float {
    val range = maxValue - minValue
    return if (range == 0f) {
        bottom
    } else {
        bottom - ((value - minValue) / range) * (bottom - top)
    }
}

/**
 * Picks the concrete side a callout bubble is drawn on, turning [CalloutSide.AUTO] into
 * [CalloutSide.ABOVE] or [CalloutSide.BELOW] based on the room left above the anchor.
 *
 * An explicit preference is honoured as given, so a caller can pin a bubble even where it has to be
 * clamped against an edge.
 *
 * @param preferredSide The side requested by the callout's style.
 * @param anchorY The anchor's y-coordinate.
 * @param bubbleHeight The measured height of the bubble in pixels.
 * @param pointerLength The gap between the anchor and the nearest bubble edge.
 * @param plotTop The y-coordinate of the plot's top edge.
 * @return Either [CalloutSide.ABOVE] or [CalloutSide.BELOW]; never [CalloutSide.AUTO].
 */
internal fun resolveCalloutSide(
    preferredSide: CalloutSide,
    anchorY: Float,
    bubbleHeight: Float,
    pointerLength: Float,
    plotTop: Float,
): CalloutSide =
    when (preferredSide) {
        CalloutSide.ABOVE -> CalloutSide.ABOVE
        CalloutSide.BELOW -> CalloutSide.BELOW
        CalloutSide.AUTO ->
            if (anchorY - pointerLength - bubbleHeight >= plotTop) {
                CalloutSide.ABOVE
            } else {
                CalloutSide.BELOW
            }
    }

/**
 * Horizontally centres a bubble of [bubbleWidth] on [anchorX] and keeps it inside the plot.
 *
 * @param anchorX The anchor's x-coordinate.
 * @param bubbleWidth The measured width of the bubble in pixels.
 * @param plotLeft The x-coordinate of the plot's left edge.
 * @param plotRight The x-coordinate of the plot's right edge.
 * @return The bubble's left x-coordinate.
 */
internal fun calloutBubbleLeft(
    anchorX: Float,
    bubbleWidth: Float,
    plotLeft: Float,
    plotRight: Float,
): Float {
    val maxLeft = plotRight - bubbleWidth
    return if (maxLeft <= plotLeft) {
        plotLeft
    } else {
        (anchorX - bubbleWidth / HALF).coerceIn(plotLeft, maxLeft)
    }
}

/**
 * Places a bubble of [bubbleHeight] on the given [side] of [anchorY] and keeps it inside the plot.
 *
 * @param anchorY The anchor's y-coordinate.
 * @param bubbleHeight The measured height of the bubble in pixels.
 * @param side The resolved side, as returned by [resolveCalloutSide].
 * @param pointerLength The gap between the anchor and the nearest bubble edge.
 * @param plotTop The y-coordinate of the plot's top edge.
 * @param plotBottom The y-coordinate of the plot's bottom edge.
 * @return The bubble's top y-coordinate.
 */
internal fun calloutBubbleTop(
    anchorY: Float,
    bubbleHeight: Float,
    side: CalloutSide,
    pointerLength: Float,
    plotTop: Float,
    plotBottom: Float,
): Float {
    val raw =
        if (side == CalloutSide.ABOVE) {
            anchorY - pointerLength - bubbleHeight
        } else {
            anchorY + pointerLength
        }
    val maxTop = plotBottom - bubbleHeight
    return if (maxTop <= plotTop) {
        plotTop
    } else {
        raw.coerceIn(plotTop, maxTop)
    }
}
