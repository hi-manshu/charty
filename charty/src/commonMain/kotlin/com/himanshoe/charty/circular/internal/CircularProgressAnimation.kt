package com.himanshoe.charty.circular.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.circular.data.CircularRingData
import com.himanshoe.charty.common.config.Animation

/**
 * Remember and animate progress values for all rings
 */
@Composable
internal fun rememberAnimatedProgress(
    ringsList: List<CircularRingData>,
    animation: Animation,
): List<Float> {
    return ringsList.fastMap { ring ->
        val targetProgress = ring.progress.coerceIn(0f, ring.maxValue)
        when (animation) {
            is Animation.Disabled -> targetProgress
            is Animation.Enabled -> {
                val animatedValue = remember { Animatable(0f) }
                LaunchedEffect(targetProgress) {
                    animatedValue.animateTo(
                        targetValue = targetProgress,
                        animationSpec = tween(
                            durationMillis = animation.duration,
                            easing = FastOutSlowInEasing,
                        ),
                    )
                }
                animatedValue.value
            }
        }
    }
}

