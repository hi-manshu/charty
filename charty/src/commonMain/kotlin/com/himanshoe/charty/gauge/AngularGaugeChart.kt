package com.himanshoe.charty.gauge

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.animation.isAnimated
import com.himanshoe.charty.common.animation.toFloatSpec
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.gauge.config.AngularGaugeConfig
import com.himanshoe.charty.gauge.internal.gaugeAngleForValue
import com.himanshoe.charty.gauge.internal.gaugeBandArc
import com.himanshoe.charty.gauge.internal.gaugeFraction
import com.himanshoe.charty.gauge.internal.gaugeTickValues
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val HALF_DIVIDER = 2f
private const val DOUBLE_MULTIPLIER = 2f
private const val DEGREES_TO_RADIANS = PI / 180.0
private const val PERCENT_MULTIPLIER = 100f
private const val RADIUS_FRACTION = 0.72f
private const val TICK_GAP_FRACTION = 0.02f
private const val TICK_LENGTH_FRACTION = 0.06f
private const val TICK_STROKE_FRACTION = 0.008f
private const val TICK_LABEL_OFFSET_FRACTION = 0.12f
private const val BAND_WIDTH_FACTOR = 0.5f
private const val BAND_GAP_FRACTION = 0.015f
private const val NEEDLE_TIP_CLEARANCE_FRACTION = 0.04f
private const val NEEDLE_TAIL_FRACTION = 0.08f
private const val NEEDLE_BASE_HALF_WIDTH_FRACTION = 0.025f
private const val PIVOT_RADIUS_FRACTION = 0.045f
private const val VALUE_LABEL_OFFSET_FRACTION = 0.4f

/** Parameters shared by the gauge drawing helpers. */
private data class GaugeDrawParams(
    val displayedValue: Float,
    val progressColor: ChartyColor,
    val config: AngularGaugeConfig,
    val ticks: List<GaugeTick>,
    val textMeasurer: TextMeasurer,
)

/** A major tick resolved once per configuration: its dial direction and its pre-measured label. */
private data class GaugeTick(
    val direction: Offset,
    val label: TextLayoutResult,
)

/** Resolved pixel geometry of the dial for the current canvas size. */
private data class GaugeGeometry(
    val center: Offset,
    val half: Float,
    val outerRadius: Float,
    val trackWidth: Float,
    val arcRadius: Float,
)

/**
 * Angular Gauge Chart - Display a single value as a speedometer-style dial
 *
 * A non-Cartesian gauge that renders a background track arc, optional colored plot bands, major
 * ticks with labels, an animated progress arc, and a tapered needle sweeping from the minimum value
 * to the current value. Ideal for KPIs, utilization meters, and speed/level indicators.
 *
 * Features:
 * - Configurable dial geometry (start angle, sweep, track width)
 * - Colored plot bands for qualitative zones (safe/warning/critical)
 * - Major ticks with formatted labels
 * - Needle animated with the shared [com.himanshoe.charty.common.config.Animation] configuration
 * - Optional formatted value label below the pivot
 *
 * Usage:
 * ```kotlin
 * AngularGaugeChart(
 *     value = { 62f },
 *     modifier = Modifier.size(300.dp),
 *     color = ChartyColor.Solid(Color(0xFF2962FF)),
 *     config = AngularGaugeConfig(
 *         minValue = 0f,
 *         maxValue = 100f,
 *         plotBands = listOf(
 *             GaugeBand(fromValue = 0f, toValue = 60f, color = ChartyColor.Solid(Color(0xFF43A047))),
 *             GaugeBand(fromValue = 60f, toValue = 85f, color = ChartyColor.Solid(Color(0xFFF9A825))),
 *             GaugeBand(fromValue = 85f, toValue = 100f, color = ChartyColor.Solid(Color(0xFFE53935))),
 *         ),
 *     ),
 * )
 * ```
 *
 * @param value Lambda returning the current value to display; it is clamped into the configured
 *   `[minValue, maxValue]` range.
 * @param modifier Modifier for the chart.
 * @param color Color of the progress arc that fills the track up to the current value.
 * @param config Configuration for the gauge's range, dial geometry, bands, needle, and animation,
 *   defined by an [AngularGaugeConfig].
 * @param accessibilityDescription Overrides the auto-generated screen-reader description. Pass an
 *   empty string to suppress it.
 */
@Composable
fun AngularGaugeChart(
    value: () -> Float,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    config: AngularGaugeConfig = AngularGaugeConfig(),
    accessibilityDescription: String? = null,
) {
    val currentValue by remember(value) { derivedStateOf { value() } }
    val targetValue = currentValue.coerceIn(config.minValue, config.maxValue)
    val chartDescription =
        remember(targetValue, config, accessibilityDescription) {
            when (accessibilityDescription) {
                "" -> null
                null -> buildGaugeDescription(value = targetValue, config = config)
                else -> accessibilityDescription
            }
        }
    val semanticsModifier =
        if (chartDescription != null) {
            Modifier.semantics { contentDescription = chartDescription }
        } else {
            Modifier
        }
    val animatedValue = remember(config.minValue, config.maxValue) { Animatable(config.minValue) }
    LaunchedEffect(animatedValue, targetValue, config.animation) {
        if (config.animation.isAnimated) {
            animatedValue.animateTo(targetValue = targetValue, animationSpec = config.animation.toFloatSpec())
        } else {
            animatedValue.snapTo(targetValue)
        }
    }
    val textMeasurer = rememberTextMeasurer()
    val ticks = rememberGaugeTicks(config = config, textMeasurer = textMeasurer)
    Box(
        modifier = modifier.then(semanticsModifier),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGauge(
                params =
                    GaugeDrawParams(
                        displayedValue = animatedValue.value,
                        progressColor = color,
                        config = config,
                        ticks = ticks,
                        textMeasurer = textMeasurer,
                    ),
            )
        }
    }
}

/**
 * Resolves the dial's major ticks once per configuration: their tick values, dial directions, and
 * measured labels, so the draw phase neither formats nor measures text on every animation frame.
 */
@Composable
private fun rememberGaugeTicks(
    config: AngularGaugeConfig,
    textMeasurer: TextMeasurer,
): List<GaugeTick> =
    remember(config, textMeasurer) {
        gaugeTickValues(
            minValue = config.minValue,
            maxValue = config.maxValue,
            tickCount = config.tickCount,
        ).map { tickValue ->
            GaugeTick(
                direction =
                    gaugeDirection(
                        gaugeAngleForValue(
                            value = tickValue,
                            minValue = config.minValue,
                            maxValue = config.maxValue,
                            startAngleDegrees = config.startAngleDegrees,
                            sweepAngleDegrees = config.sweepAngleDegrees,
                        ),
                    ),
                label =
                    textMeasurer.measure(
                        text = config.tickLabelFormatter(tickValue),
                        style = config.tickLabelTextStyle,
                    ),
            )
        }
    }

/** Builds the default screen-reader description for the gauge. */
private fun buildGaugeDescription(
    value: Float,
    config: AngularGaugeConfig,
): String {
    val fraction = gaugeFraction(value = value, minValue = config.minValue, maxValue = config.maxValue)
    val percent = (fraction * PERCENT_MULTIPLIER).roundToInt()
    return "Angular gauge showing ${config.valueFormatter(value)} in range " +
        "${config.valueFormatter(config.minValue)} to ${config.valueFormatter(config.maxValue)}, " +
        "$percent percent of the dial"
}

private fun DrawScope.drawGauge(params: GaugeDrawParams) {
    val half = size.minDimension / HALF_DIVIDER
    val outerRadius = half * RADIUS_FRACTION
    val trackWidth = outerRadius * params.config.trackWidthFraction
    val geometry =
        GaugeGeometry(
            center = Offset(x = size.width / HALF_DIVIDER, y = size.height / HALF_DIVIDER),
            half = half,
            outerRadius = outerRadius,
            trackWidth = trackWidth,
            arcRadius = outerRadius - trackWidth / HALF_DIVIDER,
        )
    drawTrack(geometry = geometry, params = params)
    drawPlotBands(geometry = geometry, params = params)
    drawProgressArc(geometry = geometry, params = params)
    drawTicks(geometry = geometry, params = params)
    drawNeedle(geometry = geometry, params = params)
    drawValueLabel(geometry = geometry, params = params)
}

private fun DrawScope.drawTrack(
    geometry: GaugeGeometry,
    params: GaugeDrawParams,
) {
    drawDialArc(
        geometry = geometry,
        brush = params.config.trackColor.toGaugeBrush(),
        startAngleDegrees = params.config.startAngleDegrees,
        sweepAngleDegrees = params.config.sweepAngleDegrees,
        radius = geometry.arcRadius,
        strokeWidth = geometry.trackWidth,
    )
}

private fun DrawScope.drawPlotBands(
    geometry: GaugeGeometry,
    params: GaugeDrawParams,
) {
    val config = params.config
    val bandWidth = geometry.trackWidth * BAND_WIDTH_FACTOR
    val bandRadius =
        geometry.arcRadius - geometry.trackWidth / HALF_DIVIDER -
            geometry.half * BAND_GAP_FRACTION - bandWidth / HALF_DIVIDER
    if (bandRadius <= 0f) {
        return
    }
    config.plotBands.fastForEach { band ->
        val arc =
            gaugeBandArc(
                fromValue = band.fromValue,
                toValue = band.toValue,
                minValue = config.minValue,
                maxValue = config.maxValue,
                startAngleDegrees = config.startAngleDegrees,
                sweepAngleDegrees = config.sweepAngleDegrees,
            )
        if (arc.sweepAngleDegrees > 0f) {
            drawDialArc(
                geometry = geometry,
                brush = band.color.toGaugeBrush(),
                startAngleDegrees = arc.startAngleDegrees,
                sweepAngleDegrees = arc.sweepAngleDegrees,
                radius = bandRadius,
                strokeWidth = bandWidth,
                cap = StrokeCap.Butt,
            )
        }
    }
}

private fun DrawScope.drawProgressArc(
    geometry: GaugeGeometry,
    params: GaugeDrawParams,
) {
    val config = params.config
    val fraction =
        gaugeFraction(
            value = params.displayedValue,
            minValue = config.minValue,
            maxValue = config.maxValue,
        )
    val sweep = fraction * config.sweepAngleDegrees
    if (sweep > 0f) {
        drawDialArc(
            geometry = geometry,
            brush = params.progressColor.toGaugeBrush(),
            startAngleDegrees = config.startAngleDegrees,
            sweepAngleDegrees = sweep,
            radius = geometry.arcRadius,
            strokeWidth = geometry.trackWidth,
        )
    }
}

private fun DrawScope.drawTicks(
    geometry: GaugeGeometry,
    params: GaugeDrawParams,
) {
    val config = params.config
    val tickBrush = config.trackColor.toGaugeBrush()
    val tickStartRadius = geometry.outerRadius + geometry.half * TICK_GAP_FRACTION
    val tickEndRadius = tickStartRadius + geometry.half * TICK_LENGTH_FRACTION
    val labelRadius = tickEndRadius + geometry.half * TICK_LABEL_OFFSET_FRACTION
    params.ticks.fastForEach { tick ->
        drawLine(
            brush = tickBrush,
            start = geometry.center + tick.direction * tickStartRadius,
            end = geometry.center + tick.direction * tickEndRadius,
            strokeWidth = geometry.half * TICK_STROKE_FRACTION,
            cap = StrokeCap.Round,
        )
        val labelCenter = geometry.center + tick.direction * labelRadius
        drawText(
            textLayoutResult = tick.label,
            topLeft =
                Offset(
                    x = labelCenter.x - tick.label.size.width / HALF_DIVIDER,
                    y = labelCenter.y - tick.label.size.height / HALF_DIVIDER,
                ),
        )
    }
}

private fun DrawScope.drawNeedle(
    geometry: GaugeGeometry,
    params: GaugeDrawParams,
) {
    val config = params.config
    val angleDegrees =
        gaugeAngleForValue(
            value = params.displayedValue,
            minValue = config.minValue,
            maxValue = config.maxValue,
            startAngleDegrees = config.startAngleDegrees,
            sweepAngleDegrees = config.sweepAngleDegrees,
        )
    val direction = gaugeDirection(angleDegrees)
    val perpendicular = Offset(x = -direction.y, y = direction.x)
    val needleLength =
        (
            geometry.arcRadius - geometry.trackWidth / HALF_DIVIDER -
                geometry.half * NEEDLE_TIP_CLEARANCE_FRACTION
        ).coerceAtLeast(0f)
    val tip = geometry.center + direction * needleLength
    val tail = geometry.center - direction * (geometry.half * NEEDLE_TAIL_FRACTION)
    val baseHalfWidth = geometry.half * NEEDLE_BASE_HALF_WIDTH_FRACTION
    val needlePath =
        Path().apply {
            moveTo(x = tip.x, y = tip.y)
            lineTo(
                x = tail.x + perpendicular.x * baseHalfWidth,
                y = tail.y + perpendicular.y * baseHalfWidth,
            )
            lineTo(
                x = tail.x - perpendicular.x * baseHalfWidth,
                y = tail.y - perpendicular.y * baseHalfWidth,
            )
            close()
        }
    val needleBrush = config.needleColor.toGaugeBrush()
    drawPath(path = needlePath, brush = needleBrush)
    drawCircle(
        brush = needleBrush,
        radius = geometry.half * PIVOT_RADIUS_FRACTION,
        center = geometry.center,
    )
}

private fun DrawScope.drawValueLabel(
    geometry: GaugeGeometry,
    params: GaugeDrawParams,
) {
    if (!params.config.showValueLabel) {
        return
    }
    val layout =
        params.textMeasurer.measure(
            text = params.config.valueFormatter(params.displayedValue),
            style = params.config.valueTextStyle,
        )
    val labelCenterY = geometry.center.y + geometry.half * VALUE_LABEL_OFFSET_FRACTION
    drawText(
        textLayoutResult = layout,
        topLeft =
            Offset(
                x = geometry.center.x - layout.size.width / HALF_DIVIDER,
                y = labelCenterY - layout.size.height / HALF_DIVIDER,
            ),
    )
}

@Suppress("LongParameterList")
private fun DrawScope.drawDialArc(
    geometry: GaugeGeometry,
    brush: Brush,
    startAngleDegrees: Float,
    sweepAngleDegrees: Float,
    radius: Float,
    strokeWidth: Float,
    cap: StrokeCap = StrokeCap.Round,
) {
    drawArc(
        brush = brush,
        startAngle = startAngleDegrees,
        sweepAngle = sweepAngleDegrees,
        useCenter = false,
        topLeft = Offset(x = geometry.center.x - radius, y = geometry.center.y - radius),
        size = Size(width = radius * DOUBLE_MULTIPLIER, height = radius * DOUBLE_MULTIPLIER),
        style = Stroke(width = strokeWidth, cap = cap),
    )
}

/** Unit direction vector for [angleDegrees] in the Compose drawing convention. */
private fun gaugeDirection(angleDegrees: Float): Offset {
    val angleRadians = angleDegrees * DEGREES_TO_RADIANS
    return Offset(x = cos(angleRadians).toFloat(), y = sin(angleRadians).toFloat())
}

/** Resolves a [ChartyColor] to a [Brush] for painting a gauge element (solid or gradient). */
private fun ChartyColor.toGaugeBrush(): Brush =
    when (this) {
        is ChartyColor.Solid -> SolidColor(color)
        is ChartyColor.Gradient -> Brush.linearGradient(colors)
    }
