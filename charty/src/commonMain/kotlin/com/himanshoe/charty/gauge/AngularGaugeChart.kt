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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.animation.isAnimated
import com.himanshoe.charty.common.animation.toFloatSpec
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.gauge.config.AngularGaugeConfig
import com.himanshoe.charty.gauge.internal.GaugeDrawParams
import com.himanshoe.charty.gauge.internal.GaugeTick
import com.himanshoe.charty.gauge.internal.buildGaugeDescription
import com.himanshoe.charty.gauge.internal.drawGauge
import com.himanshoe.charty.gauge.internal.gaugeAngleForValue
import com.himanshoe.charty.gauge.internal.gaugeDirection
import com.himanshoe.charty.gauge.internal.gaugeTickValues

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
    val displayedValue =
        if (config.animation.isAnimated) {
            animatedValue.value
        } else {
            targetValue
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
                        displayedValue = displayedValue,
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
