package com.himanshoe.charty.circular.internal

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.circular.config.CircularProgressConfig
import com.himanshoe.charty.circular.data.CircularRingData
import kotlin.math.sqrt

/**
 * Add tap gesture detection to identify which ring was clicked
 */
internal fun Modifier.ringClickHandler(
    ringsList: List<CircularRingData>,
    config: CircularProgressConfig,
    enabled: Boolean,
    onRingClick: ((CircularRingData, Int) -> Unit)?,
): Modifier {
    if (!enabled || onRingClick == null) return this

    return this.pointerInput(ringsList) {
        detectTapGestures { offset ->
            val center = Offset(size.width / CircularProgressConstants.TWO, size.height / CircularProgressConstants.TWO)
            val dx = offset.x - center.x
            val dy = offset.y - center.y
            val distance = sqrt(dx * dx + dy * dy)
            val canvasSize = minOf(size.width, size.height)
            val radius = canvasSize / CircularProgressConstants.TWO
            val strokeWidth = calculateStrokeWidth(
                radius = radius,
                centerHoleRatio = config.centerHoleRatio,
                gapBetweenRings = config.gapBetweenRings,
                ringCount = ringsList.size,
            )
            ringsList.fastForEachIndexed { index, ring ->
                val ringRadius = calculateRingRadius(
                    index = index,
                    radius = radius,
                    gapBetweenRings = config.gapBetweenRings,
                    strokeWidth = strokeWidth,
                )
                val ringHalfStroke = strokeWidth / CircularProgressConstants.TWO

                if (distance in (ringRadius - ringHalfStroke)..(ringRadius + ringHalfStroke)) {
                    onRingClick(ring, index)
                }
            }
        }
    }
}

