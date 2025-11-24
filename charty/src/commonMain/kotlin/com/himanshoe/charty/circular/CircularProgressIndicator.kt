@file:Suppress(
    "LongMethod",
    "LongParameterList",
    "FunctionNaming",
    "CyclomaticComplexMethod",
    "WildcardImport",
    "MagicNumber",
    "MaxLineLength",
    "ReturnCount",
    "UnusedImports",
)

package com.himanshoe.charty.circular

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.circular.config.CircularProgressConfig
import com.himanshoe.charty.circular.config.RingDirection
import com.himanshoe.charty.circular.data.CircularRingData
import com.himanshoe.charty.common.config.Animation
import kotlin.math.*

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

    // Animation for progress
    val animatedProgress =
        ringsList.map { ring ->
            val targetProgress = ring.progress.coerceIn(0f, ring.maxValue)
            when (val animConfig = config.animation) {
                is Animation.Disabled -> targetProgress
                is Animation.Enabled -> {
                    val animatedValue = remember { Animatable(0f) }
                    LaunchedEffect(targetProgress) {
                        animatedValue.animateTo(
                            targetValue = targetProgress,
                            animationSpec =
                                tween(
                                    durationMillis = animConfig.duration,
                                    easing = FastOutSlowInEasing,
                                ),
                        )
                    }
                    animatedValue.value
                }
            }
        }

    // Rotation animation if enabled
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

    // Track selected ring
    var selectedRingIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier.padding(config.paddingDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(
                        if (config.interactionEnabled && onRingClick != null) {
                            Modifier.pointerInput(ringsList) {
                                detectTapGestures { offset ->
                                    // Detect which ring was clicked
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    val distance = sqrt(dx * dx + dy * dy)

                                    // Calculate which ring was tapped based on distance from center
                                    val canvasSize = minOf(size.width, size.height)
                                    val radius = canvasSize / 2f

                                    // Calculate stroke width
                                    val strokeWidth =
                                        calculateStrokeWidth(
                                            radius = radius,
                                            config = config,
                                            ringCount = ringsList.size,
                                        )

                                    ringsList.forEachIndexed { index, ring ->
                                        val ringRadius =
                                            calculateRingRadius(
                                                index = index,
                                                totalRings = ringsList.size,
                                                radius = radius,
                                                config = config,
                                                strokeWidth = strokeWidth,
                                            )
                                        val ringHalfStroke = strokeWidth / 2f

                                        if (distance in (ringRadius - ringHalfStroke)..(ringRadius + ringHalfStroke)) {
                                            selectedRingIndex = index
                                            onRingClick(ring, index)
                                        }
                                    }
                                }
                            }
                        } else {
                            Modifier
                        },
                    ),
        ) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Calculate stroke width based on available space
            val strokeWidth =
                calculateStrokeWidth(
                    radius = radius,
                    config = config,
                    ringCount = ringsList.size,
                )

            // Draw each ring
            ringsList.fastForEachIndexed { index, ring ->
                val ringRadius =
                    calculateRingRadius(
                        index = index,
                        totalRings = ringsList.size,
                        radius = radius,
                        config = config,
                        strokeWidth = strokeWidth,
                    )

                val animProgress =
                    if (index < animatedProgress.size) {
                        animatedProgress[index]
                    } else {
                        ring.progress
                    }

                // Draw background ring (unfilled portion)
                drawRingBackground(
                    center = center,
                    radius = ringRadius,
                    ring = ring,
                    config = config,
                    rotationAngle = rotationAngle,
                    strokeWidth = strokeWidth,
                )

                // Draw progress ring (filled portion)
                drawRingProgress(
                    center = center,
                    radius = ringRadius,
                    ring = ring,
                    progress = animProgress,
                    config = config,
                    rotationAngle = rotationAngle,
                    isSelected = selectedRingIndex == index,
                    strokeWidth = strokeWidth,
                )
            }
        }

        // Center content
        if (centerContent != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
                content = centerContent,
            )
        } else if (config.showCenterText && ringsList.isNotEmpty()) {
            // Default center text showing first ring's percentage
            val firstRing = ringsList.first()
            val percentage =
                remember(animatedProgress.firstOrNull()) {
                    val progress = animatedProgress.firstOrNull() ?: firstRing.progress
                    ((progress / firstRing.maxValue) * 100f).toInt()
                }

            Text(
                text = "$percentage%",
                style = config.centerTextStyle,
            )
        }
    }
}

/**
 * Calculate the stroke width for each ring based on available space
 */
private fun calculateStrokeWidth(
    radius: Float,
    config: CircularProgressConfig,
    ringCount: Int,
): Float {
    if (ringCount == 0) return 0f

    val centerHoleSize = radius * config.centerHoleRatio
    val availableRadius = radius - centerHoleSize
    val totalGapSpace = config.gapBetweenRings * (ringCount - 1)
    val availableForStrokes = availableRadius - totalGapSpace

    return (availableForStrokes / ringCount).coerceAtLeast(1f)
}

/**
 * Calculate the radius for a specific ring based on its index
 */
private fun calculateRingRadius(
    index: Int,
    @Suppress("UNUSED_PARAMETER") totalRings: Int,
    radius: Float,
    config: CircularProgressConfig,
    strokeWidth: Float,
): Float {
    // Calculate position for this ring (outermost first)
    val accumulatedWidth = index * (strokeWidth + config.gapBetweenRings)

    // Return radius from outer edge
    return radius - accumulatedWidth - (strokeWidth / 2f)
}

/**
 * Draw the background ring (unfilled portion)
 */
private fun DrawScope.drawRingBackground(
    center: Offset,
    radius: Float,
    ring: CircularRingData,
    config: CircularProgressConfig,
    rotationAngle: Float,
    strokeWidth: Float,
) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val size = Size(radius * 2f, radius * 2f)

    drawArc(
        color = ring.getBackgroundColor(),
        startAngle = config.startAngleDegrees + rotationAngle,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = size,
        style =
            Stroke(
                width = strokeWidth,
                cap = config.strokeCap,
            ),
    )
}

/**
 * Draw the progress ring (filled portion) with optional shadow
 */
private fun DrawScope.drawRingProgress(
    center: Offset,
    radius: Float,
    ring: CircularRingData,
    progress: Float,
    config: CircularProgressConfig,
    rotationAngle: Float,
    @Suppress("UNUSED_PARAMETER") isSelected: Boolean,
    strokeWidth: Float,
) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val size = Size(radius * 2f, radius * 2f)

    val sweepAngle = ((progress / ring.maxValue) * 360f).coerceIn(0f, 360f)
    val actualSweepAngle =
        if (config.ringDirection == RingDirection.CLOCKWISE) {
            sweepAngle
        } else {
            -sweepAngle
        }

    if (sweepAngle > 0f) {
        // Draw shadow layers if enabled (using multiple blurred arcs for cross-platform compatibility)
        if (config.enableShadows && ring.shadowColor != null && ring.shadowRadius > 0f) {
            // Draw multiple shadow layers for a blur effect
            val shadowLayers = 4
            for (i in shadowLayers downTo 1) {
                val shadowAlpha = (0.15f / i)
                val shadowExpand = (ring.shadowRadius * i) / shadowLayers.toFloat()

                drawArc(
                    color = ring.shadowColor.copy(alpha = shadowAlpha),
                    startAngle = config.startAngleDegrees + rotationAngle,
                    sweepAngle = actualSweepAngle,
                    useCenter = false,
                    topLeft = Offset(topLeft.x - shadowExpand / 2f, topLeft.y - shadowExpand / 2f),
                    size = Size(size.width + shadowExpand, size.height + shadowExpand),
                    style =
                        Stroke(
                            width = strokeWidth + shadowExpand,
                            cap = config.strokeCap,
                        ),
                    blendMode = BlendMode.Multiply,
                )
            }
        }

        // Draw the main progress arc
        drawArc(
            color = ring.getPrimaryColor(),
            startAngle = config.startAngleDegrees + rotationAngle,
            sweepAngle = actualSweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style =
                Stroke(
                    width = strokeWidth,
                    cap = config.strokeCap,
                ),
        )
    }
}
