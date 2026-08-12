package com.himanshoe.charty.circular.rings

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.circular.rings.config.ActivityRingsConfig
import com.himanshoe.charty.circular.rings.data.RingData
import com.himanshoe.charty.circular.rings.internal.RingLayout
import com.himanshoe.charty.circular.rings.internal.buildActivityRingsDescription
import com.himanshoe.charty.circular.rings.internal.resolveRingColor
import com.himanshoe.charty.circular.rings.internal.ringLayouts
import com.himanshoe.charty.circular.rings.internal.ringSweepFraction
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.animation.rememberChartAnimation
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val HALF_DIVIDER = 2f
private const val FULL_CIRCLE_DEGREES = 360f
private const val DEGREES_TO_RADIANS = PI / 180.0
private val RING_RED = Color(0xFFFA114F)
private val RING_GREEN = Color(0xFF92E82A)
private val RING_CYAN = Color(0xFF1EEAEF)

private val DEFAULT_RING_PALETTE =
    ChartyColor.Gradient(
        colors = listOf(RING_RED, RING_GREEN, RING_CYAN),
    )

/**
 * A composable function that displays a solid gauge as concentric activity rings.
 *
 * Each [RingData] is drawn as one ring, outside-in: the first item is the outermost ring. A ring's
 * sweep fraction is `(value / target).coerceAtMost(1f)`, so values beyond the target cap at exactly
 * one full revolution rather than overlapping themselves. Behind every ring a faint full-circle
 * track is drawn at [ActivityRingsConfig.trackAlpha] so the remaining distance to the target stays
 * visible. Sweeps animate from zero according to [ActivityRingsConfig.animation].
 *
 * @param data A lambda returning the list of [RingData] to display; the first item becomes the
 *   outermost ring. An empty list renders the standard [ChartEmptyState] placeholder.
 * @param modifier The modifier applied to the chart canvas; size it square for circular rings.
 * @param colors Fallback palette for rings whose [RingData.color] is `null`: a [ChartyColor.Solid]
 *   colors every ring the same, a [ChartyColor.Gradient] assigns its colors per ring, cycling when
 *   there are more rings than colors. Defaults to a three-color activity palette.
 * @param config Appearance and behavior configuration, see [ActivityRingsConfig].
 *
 * Example usage:
 * ```kotlin
 * ActivityRingsChart(
 *     data = {
 *         listOf(
 *             RingData(label = "Move", value = 450f, target = 600f),
 *             RingData(label = "Exercise", value = 32f, target = 30f),
 *             RingData(label = "Stand", value = 9f, target = 12f),
 *         )
 *     },
 *     modifier = Modifier.size(280.dp),
 *     config = ActivityRingsConfig(
 *         ringThicknessFraction = 0.14f,
 *         animation = Animation.Slow,
 *     ),
 * )
 * ```
 */
@Composable
fun ActivityRingsChart(
    data: () -> List<RingData>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = DEFAULT_RING_PALETTE,
    config: ActivityRingsConfig = ActivityRingsConfig(),
) {
    val ringsList by remember(data) { derivedStateOf { data() } }
    if (ringsList.isEmpty()) {
        ChartEmptyState(modifier = modifier)
        return
    }
    val chartDescription = remember(ringsList) { buildActivityRingsDescription(ringsList) }
    val ringBrushes =
        remember(ringsList, colors) {
            ringsList.fastMapIndexed { index, ring ->
                resolveRingColor(ringColor = ring.color, index = index, fallback = colors).toRingBrush()
            }
        }
    val animationProgress = rememberChartAnimation(animation = config.animation)
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier.semantics { contentDescription = chartDescription },
    ) {
        drawActivityRings(
            ringsList = ringsList,
            ringBrushes = ringBrushes,
            config = config,
            progress = animationProgress.value,
            textMeasurer = textMeasurer,
        )
    }
}

private fun DrawScope.drawActivityRings(
    ringsList: List<RingData>,
    ringBrushes: List<Brush>,
    config: ActivityRingsConfig,
    progress: Float,
    textMeasurer: TextMeasurer,
) {
    val outerRadius = size.minDimension / HALF_DIVIDER
    val center = Offset(x = size.width / HALF_DIVIDER, y = size.height / HALF_DIVIDER)
    val layouts =
        ringLayouts(
            ringCount = ringsList.size,
            outerRadius = outerRadius,
            thicknessFraction = config.ringThicknessFraction,
            ringGapFraction = config.ringGapFraction,
        )
    val cap =
        if (config.roundedCaps) {
            StrokeCap.Round
        } else {
            StrokeCap.Butt
        }
    ringsList.fastForEachIndexed { index, ring ->
        val layout = layouts[index]
        val brush = ringBrushes[index]
        val topLeft = Offset(x = center.x - layout.centerRadius, y = center.y - layout.centerRadius)
        val arcSize = Size(width = layout.centerRadius * HALF_DIVIDER, height = layout.centerRadius * HALF_DIVIDER)
        val stroke = Stroke(width = layout.strokeWidth, cap = cap)
        drawArc(
            brush = brush,
            startAngle = config.startAngleDegrees,
            sweepAngle = FULL_CIRCLE_DEGREES,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
            alpha = config.trackAlpha,
        )
        val sweepAngle =
            ringSweepFraction(value = ring.value, target = ring.target) * FULL_CIRCLE_DEGREES * progress
        if (sweepAngle > 0f) {
            drawArc(
                brush = brush,
                startAngle = config.startAngleDegrees,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        if (config.showLabels) {
            drawRingLabel(
                ring = ring,
                layout = layout,
                center = center,
                config = config,
                textMeasurer = textMeasurer,
            )
        }
    }
}

private fun DrawScope.drawRingLabel(
    ring: RingData,
    layout: RingLayout,
    center: Offset,
    config: ActivityRingsConfig,
    textMeasurer: TextMeasurer,
) {
    val text = config.labelFormatter(ring)
    if (text.isEmpty()) {
        return
    }
    val textLayoutResult = textMeasurer.measure(text = text, style = config.labelTextStyle)
    val angleRad = config.startAngleDegrees * DEGREES_TO_RADIANS
    val labelX = center.x + cos(angleRad).toFloat() * layout.centerRadius - textLayoutResult.size.width / HALF_DIVIDER
    val labelY = center.y + sin(angleRad).toFloat() * layout.centerRadius - textLayoutResult.size.height / HALF_DIVIDER
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(x = labelX, y = labelY),
    )
}

/** Resolves a [ChartyColor] to the [Brush] used for a ring's stroke (solid or sweep gradient). */
private fun ChartyColor.toRingBrush(): Brush =
    when (this) {
        is ChartyColor.Solid -> SolidColor(color)
        is ChartyColor.Gradient -> Brush.sweepGradient(colors)
    }
