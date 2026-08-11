package com.himanshoe.charty.circular.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.circular.data.CircularRingData
import com.himanshoe.charty.common.animation.isAnimated
import com.himanshoe.charty.common.animation.toFloatSpec
import com.himanshoe.charty.common.config.Animation

/**
 * Remember and animate progress values for all rings
 */
@Composable
internal fun rememberAnimatedProgress(
    ringsList: List<CircularRingData>,
    animation: Animation,
): List<Float> =
    ringsList.fastMap { ring ->
        val targetProgress = ring.progress.coerceIn(0f, ring.maxValue)
        if (!animation.isAnimated) {
            targetProgress
        } else {
            val animatedValue = remember { Animatable(0f) }
            LaunchedEffect(targetProgress) {
                animatedValue.animateTo(
                    targetValue = targetProgress,
                    animationSpec = animation.toFloatSpec(),
                )
            }
            animatedValue.value
        }
    }
