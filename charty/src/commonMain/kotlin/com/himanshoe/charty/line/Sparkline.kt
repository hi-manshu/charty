package com.himanshoe.charty.line

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.accessibility.toReadableString
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.config.LineInterpolation
import com.himanshoe.charty.common.draw.interpolatedAreaPath
import com.himanshoe.charty.common.draw.interpolatedLinePath
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.line.config.SparklineConfig
import com.himanshoe.charty.line.ext.createAreaBrush
import com.himanshoe.charty.line.ext.createLineBrush

private const val CENTER_FRACTION = 0.5f

/**
 * A minimal inline chart of a single value series: no axes, labels, padding, or interaction — just
 * the trend, normalised to fill the entire draw area. Designed for table cells, list rows, and
 * dashboard tiles.
 *
 * An empty list draws nothing, a single value draws a flat line at mid-height, and an all-equal
 * series draws a centred flat line.
 *
 * @param data A lambda returning the raw values to plot, oldest first.
 * @param modifier Modifier applied to the sparkline; size it explicitly, e.g.
 *   `Modifier.width(120.dp).height(32.dp)`.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param color The line colour defined by [ChartyColor]; a [ChartyColor.Gradient] paints both the
 *   stroke and the fill with the gradient.
 * @param config Appearance and behaviour configuration for the sparkline.
 * @param accessibilityDescription Overrides the auto-generated screen-reader description. Pass an
 *   empty string to suppress it, which is the right choice when the sparkline merely repeats a
 *   value already announced next to it.
 *
 * Example:
 * ```kotlin
 * Sparkline(
 *     data = { cpuLoad },
 *     modifier = Modifier.width(120.dp).height(32.dp),
 *     color = ChartyColor.Solid(Color(0xFF2962FF)),
 * )
 * ```
 */
@Composable
fun Sparkline(
    data: () -> List<Float>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    config: SparklineConfig = SparklineConfig(),
    accessibilityDescription: String? = null,
) {
    val values by remember(data) { derivedStateOf { data() } }
    if (values.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val yFractions = remember(values) { normalizeToYFractions(values) }
    val animationProgress = rememberChartAnimation(config.animation)
    val chartDescription =
        remember(values, accessibilityDescription) {
            when (accessibilityDescription) {
                "" -> null
                null -> buildSparklineDescription(values)
                else -> accessibilityDescription
            }
        }
    val semanticsModifier =
        if (chartDescription != null) {
            Modifier.semantics { contentDescription = chartDescription }
        } else {
            Modifier
        }

    Canvas(modifier = modifier.then(semanticsModifier)) {
        if (yFractions.isNotEmpty()) {
            drawSparkline(
                yFractions = yFractions,
                color = color,
                config = config,
                animationProgress = animationProgress.value,
            )
        }
    }
}

/**
 * Draws the sparkline into the full [DrawScope.size]: the optional area fill, the line path, and
 * the optional last-point dot. [animationProgress] clips the paths from the left and fades the dot
 * in, so the sparkline reveals left to right during the entry animation.
 */
private fun DrawScope.drawSparkline(
    yFractions: List<Float>,
    color: ChartyColor,
    config: SparklineConfig,
    animationProgress: Float,
) {
    val points = sparklinePoints(yFractions = yFractions, width = size.width, height = size.height)
    val lineBrush = createLineBrush(color)
    val interpolation = if (config.smoothCurve) LineInterpolation.SMOOTH else LineInterpolation.LINEAR
    clipRect(right = size.width * animationProgress) {
        if (config.showFill) {
            drawPath(
                path =
                    interpolatedAreaPath(
                        points = points,
                        baselineY = size.height,
                        interpolation = interpolation,
                    ),
                brush =
                    createAreaBrush(
                        color = color,
                        fillAlpha = config.fillAlpha,
                        chartTop = 0f,
                        chartBottom = size.height,
                    ),
                style = Fill,
            )
        }
        drawPath(
            path = interpolatedLinePath(points = points, interpolation = interpolation),
            brush = lineBrush,
            style = Stroke(width = config.lineWidth, cap = StrokeCap.Round),
        )
    }
    if (config.showLastPointDot) {
        val lastPoint = points.last()
        val radius = config.lastPointDotRadius
        drawCircle(
            brush = lineBrush,
            radius = radius,
            center =
                Offset(
                    x = lastPoint.x.coerceIn(radius, (size.width - radius).coerceAtLeast(radius)),
                    y = lastPoint.y.coerceIn(radius, (size.height - radius).coerceAtLeast(radius)),
                ),
            alpha = animationProgress.coerceIn(0f, 1f),
        )
    }
}

/**
 * Builds the screen-reader description for a sparkline: how many values it covers, where it starts
 * and ends, and its span. An empty series yields the empty-chart wording.
 */
internal fun buildSparklineDescription(values: List<Float>): String {
    if (values.isEmpty()) {
        return "Empty sparkline."
    }
    return "Sparkline, ${values.size} values from ${values.first().toReadableString()} " +
        "to ${values.last().toReadableString()}. " +
        "Range: ${values.min().toReadableString()} to ${values.max().toReadableString()}."
}

/**
 * Normalises raw [values] into vertical fractions in `[0, 1]` measured from the top of the draw
 * area: the maximum value maps to `0` (top) and the minimum to `1` (bottom). An empty list yields
 * an empty list. When the range is zero — a single value or an all-equal series — every fraction is
 * `0.5` so the line sits centred, which also guards against division by zero.
 */
internal fun normalizeToYFractions(values: List<Float>): List<Float> {
    if (values.isEmpty()) {
        return emptyList()
    }
    val maxValue = values.max()
    val range = maxValue - values.min()
    return if (range == 0f) {
        List(values.size) { CENTER_FRACTION }
    } else {
        values.map { value -> (maxValue - value) / range }
    }
}

/**
 * Lays out [yFractions] as pixel positions across the full [width] x [height] draw area, spacing
 * points evenly along x. A single fraction becomes a full-width horizontal segment so a lone value
 * still reads as a line.
 */
internal fun sparklinePoints(
    yFractions: List<Float>,
    width: Float,
    height: Float,
): List<Offset> =
    when {
        yFractions.isEmpty() -> emptyList()
        yFractions.size == 1 -> {
            val y = yFractions.first() * height
            listOf(Offset(x = 0f, y = y), Offset(x = width, y = y))
        }
        else -> {
            val stepX = width / (yFractions.size - 1)
            yFractions.mapIndexed { index, fraction ->
                Offset(x = index * stepX, y = fraction * height)
            }
        }
    }
