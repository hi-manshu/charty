package com.himanshoe.charty.gauge.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.gauge.config.AngularGaugeConfig
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

/**
 * Parameters shared by the gauge drawing helpers.
 *
 * @property displayedValue The value the dial currently renders, already animated and clamped.
 * @property progressColor Colour of the progress arc that fills the track.
 * @property config The gauge's range, dial geometry, bands, needle, and label configuration.
 * @property ticks Major ticks resolved once per configuration.
 * @property textMeasurer Measurer used for the optional value label.
 */
internal data class GaugeDrawParams(
    val displayedValue: Float,
    val progressColor: ChartyColor,
    val config: AngularGaugeConfig,
    val ticks: List<GaugeTick>,
    val textMeasurer: TextMeasurer,
)

/**
 * A major tick resolved once per configuration: its dial direction and its pre-measured label.
 *
 * @property direction Unit vector pointing from the dial centre towards the tick.
 * @property label The pre-measured tick label.
 */
internal data class GaugeTick(
    val direction: Offset,
    val label: TextLayoutResult,
)

/**
 * Resolved pixel geometry of the dial for the current canvas size.
 *
 * @property center The dial centre in canvas pixels.
 * @property half Half of the canvas' smaller dimension.
 * @property outerRadius Outer radius of the track.
 * @property trackWidth Stroke width of the track.
 * @property arcRadius Radius of the track's stroke centre line.
 */
private data class GaugeGeometry(
    val center: Offset,
    val half: Float,
    val outerRadius: Float,
    val trackWidth: Float,
    val arcRadius: Float,
)

/**
 * Draws the complete gauge: track, plot bands, progress arc, ticks, needle, and value label.
 *
 * @param params The resolved drawing parameters for the current frame.
 */
internal fun DrawScope.drawGauge(params: GaugeDrawParams) {
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
                cap = StrokeCap.Round,
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

/**
 * Builds the default screen-reader description for the gauge.
 *
 * @param value The value currently displayed by the dial.
 * @param config The gauge configuration supplying the range and value formatting.
 * @return A sentence describing the value, the range, and how full the dial is.
 */
internal fun buildGaugeDescription(
    value: Float,
    config: AngularGaugeConfig,
): String {
    val fraction = gaugeFraction(value = value, minValue = config.minValue, maxValue = config.maxValue)
    val percent = (fraction * PERCENT_MULTIPLIER).roundToInt()
    return "Angular gauge showing ${config.valueFormatter(value)} in range " +
        "${config.valueFormatter(config.minValue)} to ${config.valueFormatter(config.maxValue)}, " +
        "$percent percent of the dial"
}

/** Unit direction vector for [angleDegrees] in the Compose drawing convention. */
internal fun gaugeDirection(angleDegrees: Float): Offset {
    val angleRadians = angleDegrees * DEGREES_TO_RADIANS
    return Offset(x = cos(angleRadians).toFloat(), y = sin(angleRadians).toFloat())
}

/** Resolves a [ChartyColor] to a [Brush] for painting a gauge element (solid or gradient). */
private fun ChartyColor.toGaugeBrush(): Brush =
    when (this) {
        is ChartyColor.Solid -> SolidColor(color)
        is ChartyColor.Gradient -> Brush.linearGradient(colors)
    }
