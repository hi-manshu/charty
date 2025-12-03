package com.himanshoe.charty.point

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.common.gesture.calculateDistance
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.point.data.BubbleData
import kotlin.math.sqrt

/**
 * Default normalized size when size range is zero
 */
internal const val DEFAULT_NORMALIZED_SIZE = 0.5f

/**
 * A data class that holds information about the size of bubbles in a bubble chart.
 *
 * @property minValue The minimum y-value in the dataset.
 * @property maxValue The maximum y-value in the dataset.
 * @property minSize The minimum size value in the dataset.
 * @property maxSize The maximum size value in the dataset.
 * @property sizeRange The range of sizes (maxSize - minSize).
 */
internal data class BubbleSizeInfo(
    val minValue: Float,
    val maxValue: Float,
    val minSize: Float,
    val maxSize: Float,
    val sizeRange: Float,
)

/**
 * A data class that holds the bounds of a bubble for click detection.
 *
 * @property center The center coordinates of the bubble.
 * @property radius The radius of the bubble.
 * @property data The [BubbleData] associated with the bubble.
 */
internal data class BubbleBounds(
    val center: Offset,
    val radius: Float,
    val data: BubbleData,
)

/**
 * Calculates the [BubbleSizeInfo] from a list of [BubbleData].
 *
 * @param dataList The list of [BubbleData].
 * @return A [BubbleSizeInfo] instance containing the calculated size information.
 */
internal fun calculateBubbleSizeInfo(dataList: List<BubbleData>): BubbleSizeInfo {
    val yValues = dataList.map { it.yValue }
    val sizes = dataList.map { it.size }
    val min = sizes.minOrNull() ?: 0f
    val max = sizes.maxOrNull() ?: 1f
    return BubbleSizeInfo(
        calculateMinValue(yValues),
        calculateMaxValue(yValues),
        min,
        max,
        max - min,
    )
}

/**
 * Calculates the radius for a bubble based on its size.
 *
 * @param bubbleSize The size of the bubble.
 * @param minSize The minimum size in the dataset.
 * @param sizeRange The range of sizes in the dataset.
 * @param minBubbleRadius The minimum allowed radius for a bubble.
 * @param maxBubbleRadius The maximum allowed radius for a bubble.
 * @return The calculated radius for the bubble.
 */
internal fun calculateBubbleRadius(
    bubbleSize: Float,
    minSize: Float,
    sizeRange: Float,
    minBubbleRadius: Float,
    maxBubbleRadius: Float,
): Float {
    val normalizedSize = if (sizeRange > 0f) {
        (bubbleSize - minSize) / sizeRange
    } else {
        DEFAULT_NORMALIZED_SIZE
    }
    val radiusRange = maxBubbleRadius - minBubbleRadius
    return minBubbleRadius + (sqrt(normalizedSize) * radiusRange)
}

/**
 * Creates a click modifier for a bubble chart.
 *
 * @param dataList The list of [BubbleData].
 * @param bubbleBounds The list of [BubbleBounds] for click detection.
 * @param onBubbleClick A lambda function to be invoked when a bubble is clicked.
 * @return A [Modifier] that handles tap gestures for the bubble chart.
 */
internal fun createBubbleClickModifier(
    dataList: List<BubbleData>,
    bubbleBounds: List<BubbleBounds>,
    onBubbleClick: ((BubbleData) -> Unit)?,
): Modifier {
    return if (onBubbleClick != null) {
        Modifier.pointerInput(dataList, onBubbleClick) {
            detectTapGestures { tapOffset ->
                val clickedBubble = bubbleBounds.find { bubble ->
                    val distance = calculateDistance(bubble.center, tapOffset)
                    distance <= bubble.radius
                }
                clickedBubble?.let { bubble ->
                    onBubbleClick.invoke(bubble.data)
                }
            }
        }
    } else {
        Modifier
    }
}

