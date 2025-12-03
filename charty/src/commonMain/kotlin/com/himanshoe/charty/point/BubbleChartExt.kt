package com.himanshoe.charty.point

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.common.gesture.calculateDistance
import com.himanshoe.charty.point.data.BubbleData
import kotlin.math.sqrt

/**
 * Default normalized size when size range is zero
 */
internal const val DEFAULT_NORMALIZED_SIZE = 0.5f

/**
 * Data class to hold bubble size information
 */
internal data class BubbleSizeInfo(
    val minValue: Float,
    val maxValue: Float,
    val minSize: Float,
    val maxSize: Float,
    val sizeRange: Float,
)

/**
 * Data class to hold bubble bounds for click detection
 */
internal data class BubbleBounds(
    val center: Offset,
    val radius: Float,
    val data: BubbleData,
)

/**
 * Calculate bubble size information from data list
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
 * Calculate the radius for a bubble based on its size
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
 * Create click modifier for bubble chart
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

