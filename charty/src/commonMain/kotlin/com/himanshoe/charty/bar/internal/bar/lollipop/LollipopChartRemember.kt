package com.himanshoe.charty.bar.internal.bar.lollipop

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.util.calculateMaxValue

/**
 * Remembers the value range (min, max) for the lollipop chart.
 */
@Composable
internal fun rememberLollipopValueRange(dataList: List<BarData>): Pair<Float, Float> {
    return remember(dataList) {
        val values = dataList.getValues()
        0f to calculateMaxValue(values)
    }
}

/**
 * Remembers the animation progress for lollipop chart.
 */
@Composable
internal fun rememberLollipopAnimation(animation: Animation): Animatable<Float, *> {
    return rememberChartAnimation(animation)
}

