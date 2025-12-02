package com.himanshoe.charty.bar.internal.bar.horizontal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.ext.calculateMaxValue
import com.himanshoe.charty.bar.ext.calculateMinValue
import com.himanshoe.charty.bar.ext.getValues
import com.himanshoe.charty.common.config.Animation

@Composable
internal fun rememberHorizontalValueRange(
    dataList: List<BarData>,
    negativeValuesDrawMode: NegativeValuesDrawMode
): Pair<Float, Float> {
    return remember(dataList, negativeValuesDrawMode) {
        val values = dataList.getValues()
        val calculatedMin = calculateMinValue(values)
        val calculatedMax = calculateMaxValue(values)
        val finalMin = if (calculatedMin >= 0f) 0f else calculatedMin
        finalMin to calculatedMax
    }
}

@Composable
internal fun rememberHorizontalAnimation(animation: Animation): Animatable<Float, *> {
    val animationProgress = remember {
        Animatable(if (animation is Animation.Enabled) 0f else 1f)
    }

    LaunchedEffect(animation) {
        if (animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animation.duration),
            )
        }
    }

    return animationProgress
}

