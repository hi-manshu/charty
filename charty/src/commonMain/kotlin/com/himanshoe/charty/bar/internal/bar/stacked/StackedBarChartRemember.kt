package com.himanshoe.charty.bar.internal.bar.stacked

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation

@Composable
internal fun rememberStackedMaxTotal(
    dataList: List<BarGroup>,
    colors: ChartyColor
): Pair<Float, List<Color>> {
    return remember(dataList, colors) {
        val totals = dataList.fastMap { group -> group.values.sum() }
        (totals.maxOrNull() ?: 0f) to colors.value
    }
}

@Composable
internal fun rememberStackedAnimation(animation: Animation): Animatable<Float, *> {
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

