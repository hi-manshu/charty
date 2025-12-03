package com.himanshoe.charty.circular

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.circular.config.CircularProgressConfig
import com.himanshoe.charty.circular.data.CircularRingData
import com.himanshoe.charty.circular.internal.CircularProgressConstants
import com.himanshoe.charty.circular.internal.calculateRingRadius
import com.himanshoe.charty.circular.internal.calculateStrokeWidth
import com.himanshoe.charty.circular.internal.drawRingBackground
import com.himanshoe.charty.circular.internal.drawRingProgress
import com.himanshoe.charty.circular.internal.rememberAnimatedProgress
import com.himanshoe.charty.circular.internal.ringClickHandler

/**
 * A composable function that displays a circular progress indicator with multiple concentric rings.
 *
 * This indicator is highly configurable and can be used to show progress for multiple data points simultaneously, similar to Apple's Activity Rings.
 * It supports customization of colors, shadows, gaps, stroke caps, and animations.
 *
 * @param rings A lambda function that returns a list of [CircularRingData], each representing a progress ring.
 * @param modifier The modifier to be applied to the indicator.
 * @param config The configuration for the circular progress indicator's appearance and behavior, defined by a [CircularProgressConfig].
 * @param onRingClick A lambda function to be invoked when a ring is clicked, providing the corresponding [CircularRingData] and its index.
 * @param centerContent A composable lambda that allows for placing content in the center of the rings.
 *
 * // Basic three-ring indicator
 * CircularProgressIndicator(
 *     rings = {
 *         listOf(
 *             CircularRingData(label = "Move", progress = 450f, maxValue = 600f, color = Color(0xFFFF3B58)),
 *             CircularRingData(label = "Exercise", progress = 25f, maxValue = 30f, color = Color(0xFFACFF3D)),
 *             CircularRingData(label = "Stand", progress = 10f, maxValue = 12f, color = Color(0xFF34D5FF))
 *         )
 *     },
 *     modifier = Modifier.size(300.dp)
 * )
 *
 * // With shadows and custom configuration
 * CircularProgressIndicator(
 *     rings = { ringDataList },
 *     modifier = Modifier.size(300.dp),
 *     config = CircularProgressConfig(
 *         gapBetweenRings = 12f,
 *         startAngleDegrees = -90f,
 *         strokeCap = StrokeCap.Round,
 *         enableShadows = true,
 *         animation = Animation.Enabled(duration = 1500)
 *     ),
 *     onRingClick = { ring, index ->
 *         println("Clicked: ${ring.label} - ${ring.calculatePercentage()}%")
 *     }
 * )
 */
@Composable
fun CircularProgressIndicator(
    rings: () -> List<CircularRingData>,
    modifier: Modifier = Modifier,
    config: CircularProgressConfig = CircularProgressConfig(),
    onRingClick: ((ring: CircularRingData, index: Int) -> Unit)? = null,
    centerContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    val ringsList = remember(rings) { rings() }
    val animatedProgress = rememberAnimatedProgress(ringsList, config.animation)

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (config.rotationEnabled) 360f else 0f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = config.rotationDurationMs,
                        easing = LinearEasing,
                    ),
                repeatMode = RepeatMode.Restart,
            ),
        label = "rotationAngle",
    )


    Box(
        modifier = modifier.padding(config.paddingDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .ringClickHandler(
                    ringsList = ringsList,
                    config = config,
                    enabled = config.interactionEnabled,
                    onRingClick = onRingClick,
                ),
        ) {
            val canvasSize = size.minDimension
            val radius = canvasSize / CircularProgressConstants.TWO
            val center = Offset(size.width / CircularProgressConstants.TWO, size.height / CircularProgressConstants.TWO)
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

                val animProgress = if (index < animatedProgress.size) {
                    animatedProgress[index]
                } else {
                    ring.progress
                }
                drawRingBackground(
                    center = center,
                    radius = ringRadius,
                    ring = ring,
                    config = config,
                    rotationAngle = rotationAngle,
                    strokeWidth = strokeWidth,
                )
                drawRingProgress(
                    center = center,
                    radius = ringRadius,
                    ring = ring,
                    progress = animProgress,
                    config = config,
                    rotationAngle = rotationAngle,
                    strokeWidth = strokeWidth,
                )
            }
        }
        if (centerContent != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
                content = centerContent,
            )
        } else if (config.showCenterText && ringsList.isNotEmpty()) {
            val firstRing = ringsList.first()
            val percentage = remember(animatedProgress.firstOrNull()) {
                val progress = animatedProgress.firstOrNull() ?: firstRing.progress
                ((progress / firstRing.maxValue) * CircularProgressConstants.PERCENTAGE_MULTIPLIER).toInt()
            }

            Text(
                text = "$percentage%",
                style = config.centerTextStyle,
            )
        }
    }
}

