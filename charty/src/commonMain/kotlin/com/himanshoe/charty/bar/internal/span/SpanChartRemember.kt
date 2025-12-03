package com.himanshoe.charty.bar.internal.span

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.config.Animation

@Composable
internal fun rememberSpanValueRange(
    dataList: List<SpanData>,
    colors: ChartyColor
): Pair<Float, Float> {
    return remember(dataList, colors) {
        val allValues = dataList.flatMap { listOf(it.startValue, it.endValue) }
        Pair(
            allValues.minOrNull() ?: 0f,
            allValues.maxOrNull() ?: 100f,
        )
    }
}

@Composable
internal fun rememberSpanAnimation(animation: Animation): Animatable<Float, *> {
    return rememberChartAnimation(animation)
}

