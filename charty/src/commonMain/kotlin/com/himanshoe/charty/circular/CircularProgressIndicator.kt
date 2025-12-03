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
 * CircularProgressIndicator - Display multiple concentric progress rings (like Apple Activity Rings)
 *
 * A highly configurable circular progress indicator that supports:
 * - Multiple concentric rings with independent progress
 * - Customizable colors for each ring (filled and background)
 * - Shadow effects for depth and visual appeal
 * - Configurable gaps between rings
 * - Smooth animations
 * - Click interactions
 * - Round or square stroke caps
 *
 * Performance Features:
 * - Efficient canvas rendering
 * - Optimized shadow drawing
 * - Smart recomposition scoping
 *
 * Usage:
 * ```kotlin
 * // Basic three-ring indicator (like Apple Activity Rings)
 * CircularProgressIndicator(
 *     rings = {
 *         listOf(
 *             CircularRingData(
 *                 label = "Move",
 *                 progress = 450f,
 *                 maxValue = 600f,
 *                 color = Color(0xFFFF3B58),
 *                 backgroundColor = Color(0x33FF3B58),
 *                 strokeWidth = 24f
 *             ),
 *             CircularRingData(
 *                 label = "Exercise",
 *                 progress = 25f,
 *                 maxValue = 30f,
 *                 color = Color(0xFFACFF3D),
 *                 backgroundColor = Color(0x33ACFF3D),
 *                 strokeWidth = 24f
 *             ),
 *             CircularRingData(
 *                 label = "Stand",
 *                 progress = 10f,
 *                 maxValue = 12f,
 *                 color = Color(0xFF34D5FF),
 *                 backgroundColor = Color(0x3334D5FF),
 *                 strokeWidth = 24f
 *             )
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
 * ```
 *
 * @param rings Lambda returning list of ring data (from outermost to innermost)
 * @param modifier Modifier for the indicator
 * @param config Configuration for appearance and behavior
 * @param onRingClick Callback invoked when a ring is clicked (receives CircularRingData and index)
 * @param centerContent Optional composable content for the center of the rings
 */
@Composable
fun CircularProgressIndicator(
    rings: () -> List<CircularRingData>,
    modifier: Modifier = Modifier,
    config: CircularProgressConfig = CircularProgressConfig(),
    onRingClick: ((CircularRingData, Int) -> Unit)? = null,
    centerContent: @Composable (BoxScope.() -> Unit)? = null,
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

