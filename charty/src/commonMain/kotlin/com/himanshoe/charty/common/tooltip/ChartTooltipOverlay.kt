package com.himanshoe.charty.common.tooltip

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
import kotlin.math.roundToInt

private const val CENTER_DIVISOR = 2f
private const val TOOLTIP_FADE_DURATION_MS = 150

/**
 * Renders a caller-supplied composable as a chart tooltip, positioned over the chart.
 *
 * This is the Compose-based counterpart to the canvas tooltip drawn by
 * [drawTooltip][com.himanshoe.charty.common.tooltip.drawTooltip]. When a chart is given a
 * `tooltipContent` slot, it places this overlay on top of its drawing surface and lets the caller
 * render any layout they like for [item]. Placement mirrors the canvas tooltip: the content is
 * horizontally centred over the selected bar/point, clamped to stay [TooltipConfig.minDistanceFromEdge]
 * from the chart edges, and flipped above/below the anchor according to [TooltipState.position] and
 * the available space.
 *
 * The overlay is laid out with [Modifier.matchParentSize] inside the chart's root `Box`, so its
 * coordinate space matches the anchor pixels stored in [anchor].
 *
 * @param T The chart's data/segment type.
 * @param item The data point the tooltip points to, or `null` when no tooltip is shown.
 * @param anchor The resolved tooltip anchor (position and bar width), or `null` when hidden.
 * @param config The tooltip configuration; only spacing and edge fields are used for custom content.
 * @param modifier Modifier applied to the overlay container. Charts pass [Modifier.matchParentSize].
 * @param content The caller's tooltip layout, invoked with [item].
 */
@Composable
fun <T> ChartTooltipOverlay(
    item: T?,
    anchor: TooltipState?,
    config: TooltipConfig,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    if (item == null || anchor == null) {
        return
    }

    val density = LocalDensity.current
    val offsetYPx = with(density) { config.offsetY.toPx() }
    val minEdgePx = with(density) { config.minDistanceFromEdge.toPx() }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = modifier.onSizeChanged { containerSize = it }) {
        val isMeasured = tooltipSize != IntSize.Zero && containerSize != IntSize.Zero
        val animatedAlpha by animateFloatAsState(
            targetValue =
                if (isMeasured) {
                    1f
                } else {
                    0f
                },
            animationSpec = tween(durationMillis = TOOLTIP_FADE_DURATION_MS, easing = FastOutSlowInEasing),
            label = "tooltipFade",
        )
        Box(
            modifier =
                Modifier
                    .onSizeChanged { tooltipSize = it }
                    .offset {
                        resolveTooltipOffset(
                            anchor = anchor,
                            tooltipSize = tooltipSize,
                            containerSize = containerSize,
                            offsetY = offsetYPx,
                            minEdge = minEdgePx,
                        )
                    }.graphicsLayer { alpha = animatedAlpha },
        ) {
            content(item)
        }
    }
}

private fun resolveTooltipOffset(
    anchor: TooltipState,
    tooltipSize: IntSize,
    containerSize: IntSize,
    offsetY: Float,
    minEdge: Float,
): IntOffset {
    val x =
        resolveTooltipX(
            anchor = anchor,
            tooltipWidth = tooltipSize.width.toFloat(),
            containerWidth = containerSize.width.toFloat(),
            minEdge = minEdge,
        )
    val y =
        resolveTooltipY(
            anchor = anchor,
            tooltipHeight = tooltipSize.height.toFloat(),
            containerHeight = containerSize.height.toFloat(),
            offsetY = offsetY,
            minEdge = minEdge,
        )
    return IntOffset(x.roundToInt(), y.roundToInt())
}

private fun resolveTooltipX(
    anchor: TooltipState,
    tooltipWidth: Float,
    containerWidth: Float,
    minEdge: Float,
): Float {
    val barCenterX = anchor.x + (anchor.barWidth / CENTER_DIVISOR)
    val centeredX = barCenterX - (tooltipWidth / CENTER_DIVISOR)
    return when {
        centeredX < minEdge -> minEdge
        centeredX + tooltipWidth > containerWidth - minEdge -> containerWidth - tooltipWidth - minEdge
        else -> centeredX
    }
}

private fun resolveTooltipY(
    anchor: TooltipState,
    tooltipHeight: Float,
    containerHeight: Float,
    offsetY: Float,
    minEdge: Float,
): Float {
    val aboveY = anchor.y - tooltipHeight - offsetY
    val belowY = anchor.y + offsetY
    val fitsAbove = aboveY >= minEdge
    val fitsBelow = belowY + tooltipHeight <= containerHeight - minEdge
    val placeAbove =
        when (anchor.position) {
            TooltipPosition.ABOVE -> fitsAbove
            TooltipPosition.BELOW -> !fitsBelow
            TooltipPosition.AUTO ->
                when {
                    fitsAbove -> true
                    fitsBelow -> false
                    else -> anchor.y > (containerHeight - anchor.y)
                }
        }
    return if (placeAbove) {
        aboveY
    } else {
        belowY
    }
}
