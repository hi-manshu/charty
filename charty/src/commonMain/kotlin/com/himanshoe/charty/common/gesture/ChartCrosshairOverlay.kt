package com.himanshoe.charty.common.gesture

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.common.tooltip.TooltipConfig
import kotlin.math.roundToInt

private const val CENTER_DIVISOR = 2f
private const val CROSSHAIR_FADE_DURATION_MS = 150
private val CrosshairContentGap = 12.dp

/**
 * Renders a caller-supplied composable as the crosshair label, positioned over the chart.
 *
 * This is the Compose-based counterpart to the canvas crosshair label drawn by
 * [drawPointCrosshair][com.himanshoe.charty.line.internal.line.drawPointCrosshair]. When a
 * chart is given a `crosshairContent` slot, it places this overlay on top of its drawing surface and
 * lets the caller render any layout they like for [item]. The content is horizontally centred over
 * the snapped data point ([CrosshairState.x]), clamped to stay [TooltipConfig.minDistanceFromEdge]
 * from the chart edges, and flipped above/below the point according to the available space.
 *
 * The overlay is laid out with [Modifier.matchParentSize] inside the chart's root `Box`, so its
 * coordinate space matches the crosshair pixel position stored in [state].
 *
 * @param T The chart's data/point type.
 * @param item The data item the crosshair is snapped to, or `null` when the crosshair is hidden.
 * @param state The current crosshair position, or `null` when hidden.
 * @param config The tooltip configuration; only edge spacing is used for custom content.
 * @param modifier Modifier applied to the overlay container. Charts pass [Modifier.matchParentSize].
 * @param content The caller's crosshair label layout, invoked with [item].
 */
@Composable
fun <T> ChartCrosshairOverlay(
    item: T?,
    state: CrosshairState?,
    config: TooltipConfig,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    if (item == null || state == null) {
        return
    }

    val density = LocalDensity.current
    val gapPx = with(density) { CrosshairContentGap.toPx() }
    val minEdgePx = with(density) { config.minDistanceFromEdge.toPx() }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var contentSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = modifier.onSizeChanged { containerSize = it }) {
        val isMeasured = contentSize != IntSize.Zero && containerSize != IntSize.Zero
        val animatedAlpha by animateFloatAsState(
            targetValue =
                if (isMeasured) {
                    1f
                } else {
                    0f
                },
            animationSpec = tween(durationMillis = CROSSHAIR_FADE_DURATION_MS, easing = FastOutSlowInEasing),
            label = "crosshairFade",
        )
        Box(
            modifier =
                Modifier
                    .onSizeChanged { contentSize = it }
                    .offset {
                        resolveCrosshairOffset(
                            state = state,
                            contentSize = contentSize,
                            containerSize = containerSize,
                            gap = gapPx,
                            minEdge = minEdgePx,
                        )
                    }.graphicsLayer { alpha = animatedAlpha },
        ) {
            content(item)
        }
    }
}

private fun resolveCrosshairOffset(
    state: CrosshairState,
    contentSize: IntSize,
    containerSize: IntSize,
    gap: Float,
    minEdge: Float,
): IntOffset {
    val x =
        resolveCrosshairX(
            anchorX = state.x,
            contentWidth = contentSize.width.toFloat(),
            containerWidth = containerSize.width.toFloat(),
            minEdge = minEdge,
        )
    val y =
        resolveCrosshairY(
            anchorY = state.y,
            contentHeight = contentSize.height.toFloat(),
            containerHeight = containerSize.height.toFloat(),
            gap = gap,
            minEdge = minEdge,
        )
    return IntOffset(x.roundToInt(), y.roundToInt())
}

private fun resolveCrosshairX(
    anchorX: Float,
    contentWidth: Float,
    containerWidth: Float,
    minEdge: Float,
): Float {
    val centeredX = anchorX - (contentWidth / CENTER_DIVISOR)
    return when {
        centeredX < minEdge -> minEdge
        centeredX + contentWidth > containerWidth - minEdge -> containerWidth - contentWidth - minEdge
        else -> centeredX
    }
}

private fun resolveCrosshairY(
    anchorY: Float,
    contentHeight: Float,
    containerHeight: Float,
    gap: Float,
    minEdge: Float,
): Float {
    val aboveY = anchorY - contentHeight - gap
    val belowY = anchorY + gap
    val fitsAbove = aboveY >= minEdge
    return if (fitsAbove) {
        aboveY
    } else {
        belowY.coerceAtMost(containerHeight - contentHeight - minEdge)
    }
}
