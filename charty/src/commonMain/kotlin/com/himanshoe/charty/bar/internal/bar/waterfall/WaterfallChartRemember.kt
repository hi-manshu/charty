package com.himanshoe.charty.bar.internal.bar.waterfall

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.config.Animation

/**
 * Remember cumulative values for waterfall chart
 */
@Composable
internal fun rememberCumulativeValues(items: List<BarData>): List<Float> {
    return remember(items) {
        calculateCumulativeValues(items)
    }
}

/**
 * Remember animation progress for waterfall chart
 */
@Composable
internal fun rememberWaterfallAnimationProgress(config: WaterfallChartConfig): Animatable<Float, *> {
    return remember {
        Animatable(if (config.animation is Animation.Enabled) 0f else 1f)
    }
}

