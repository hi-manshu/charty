package com.himanshoe.charty.bar.internal.bar.barchart

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue

/**
 * Remember the animation progress based on the bar configuration
 */
@Composable
internal fun rememberBarAnimationProgress(animationConfig: Animation): Animatable<Float, *> {
    return rememberChartAnimation(animationConfig)
}

/**
 * Helper function to remember the value range (min, max) for the Y axis
 */
@Composable
internal fun rememberBarValueRange(
    dataList: List<BarData>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
): Pair<Float, Float> {
    return remember(dataList, negativeValuesDrawMode) {
        val values = dataList.getValues()
        val calculatedMin = calculateMinValue(values)
        val calculatedMax = calculateMaxValue(values)
        val finalMin = if (calculatedMin >= 0f) 0f else calculatedMin
        finalMin to calculatedMax
    }
}

