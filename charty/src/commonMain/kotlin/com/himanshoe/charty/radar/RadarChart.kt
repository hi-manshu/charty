package com.himanshoe.charty.radar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.radar.config.RadarChartConfig
import com.himanshoe.charty.radar.config.RadarGridStyle
import com.himanshoe.charty.radar.data.RadarDataSet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val FULL_CIRCLE_DEGREES = 360f
private const val DEGREES_TO_RADIANS = PI.toFloat() / 180f

/**
 * A composable function that displays a radar chart, also known as a spider or web chart.
 *
 * A radar chart is a graphical method of displaying multivariate data in the form of a two-dimensional chart of three or more quantitative variables represented on axes starting from the same point.
 * It is useful for comparing multiple variables, showing performance profiles, or displaying capabilities across different dimensions.
 *
 * @param data A lambda function that returns a list of [RadarDataSet] to be displayed.
 * @param modifier The modifier to be applied to the chart.
 * @param config The configuration for the radar chart's appearance, defined by a [RadarChartConfig].
 *
 * RadarChart(
 *     data = {
 *         listOf(
 *             RadarDataSet(
 *                 label = "Player 1",
 *                 axes = listOf(
 *                     RadarAxisData("Speed", 80f),
 *                     RadarAxisData("Power", 90f),
 *                     RadarAxisData("Defense", 70f),
 *                     RadarAxisData("Skill", 85f),
 *                     RadarAxisData("Stamina", 75f)
 *                 ),
 *                 color = ChartyColor.Solid(Color.Cyan),
 *                 fillAlpha = 0.3f
 *             )
 *         )
 *     },
 *     config = RadarChartConfig(
 *         gridConfig = RadarGridConfig(
 *             gridStyle = RadarGridStyle.POLYGON,
 *             numberOfGridLevels = 5
 *         ),
 *         labelConfig = RadarLabelConfig(showLabels = true)
 *     )
 * )
 */
@Composable
fun RadarChart(
    data: () -> List<RadarDataSet>,
    modifier: Modifier = Modifier,
    config: RadarChartConfig = RadarChartConfig(),
) {
    val dataSets = remember(data) { data() }
    require(dataSets.isNotEmpty()) { "Radar chart data cannot be empty" }

    val numberOfAxes = dataSets.first().axes.size
    require(dataSets.all { it.axes.size == numberOfAxes }) {
        "All datasets must have the same number of axes"
    }

    val animationProgress = rememberChartAnimation(config.animation)
    val textMeasurer = rememberTextMeasurer()
    val axisLabels = remember(dataSets) { dataSets.first().axes.map { it.label } }

    BoxWithConstraints(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = min(centerX, centerY) * (1f - config.paddingFraction)
            if (config.gridConfig.showGridLines) {
                drawRadarGrid(
                    center = Offset(centerX, centerY),
                    maxRadius = maxRadius,
                    numberOfAxes = numberOfAxes,
                    numberOfLevels = config.gridConfig.numberOfGridLevels,
                    gridStyle = config.gridConfig.gridStyle,
                    gridLineWidth = config.gridConfig.gridLineWidth,
                    gridLineColor = config.gridConfig.gridLineColor,
                    startAngle = config.startAngleDegrees,
                )
            }

            // Draw axis lines
            if (config.gridConfig.showAxisLines) {
                drawAxisLines(
                    center = Offset(centerX, centerY),
                    maxRadius = maxRadius,
                    numberOfAxes = numberOfAxes,
                    axisLineWidth = config.gridConfig.axisLineWidth,
                    axisLineColor = config.gridConfig.axisLineColor,
                    startAngle = config.startAngleDegrees,
                )
            }

            // Draw data sets
            dataSets.fastForEachIndexed { _, dataSet ->
                drawRadarDataSet(
                    center = Offset(centerX, centerY),
                    maxRadius = maxRadius,
                    dataSet = dataSet,
                    config = config,
                    animationProgress = animationProgress.value,
                )
            }

            // Draw labels
            if (config.labelConfig.showLabels) {
                drawAxisLabels(
                    center = Offset(centerX, centerY),
                    maxRadius = maxRadius,
                    labels = axisLabels,
                    numberOfAxes = numberOfAxes,
                    config = config,
                    textMeasurer = textMeasurer,
                    startAngle = config.startAngleDegrees,
                )
            }

            // Draw center background
            if (config.centerConfig.centerBackgroundRadius > 0f) {
                drawCircle(
                    color = config.centerConfig.centerBackgroundColor,
                    radius = config.centerConfig.centerBackgroundRadius,
                    center = Offset(centerX, centerY),
                )
            }
        }
    }
}

private fun DrawScope.drawCircularGrid(
    center: Offset,
    radius: Float,
    gridLineWidth: Float,
    gridLineColor: ChartyColor,
) {
    drawCircle(
        brush = Brush.linearGradient(gridLineColor.value),
        radius = radius,
        center = center,
        style = Stroke(width = gridLineWidth),
    )
}

private fun DrawScope.drawPolygonGrid(
    center: Offset,
    radius: Float,
    numberOfAxes: Int,
    gridLineWidth: Float,
    gridLineColor: ChartyColor,
    startAngle: Float,
) {
    val path = Path()
    for (i in 0 until numberOfAxes) {
        val angle = (startAngle + (FULL_CIRCLE_DEGREES * i / numberOfAxes)) * DEGREES_TO_RADIANS
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    drawPath(
        path = path,
        brush = Brush.linearGradient(gridLineColor.value),
        style = Stroke(width = gridLineWidth),
    )
}

/**
 * Draw the radar grid (circular or polygonal)
 */
private fun DrawScope.drawRadarGrid(
    center: Offset,
    maxRadius: Float,
    numberOfAxes: Int,
    numberOfLevels: Int,
    gridStyle: RadarGridStyle,
    gridLineWidth: Float,
    gridLineColor: ChartyColor,
    startAngle: Float,
) {
    for (level in 1..numberOfLevels) {
        val radius = (maxRadius * level) / numberOfLevels

        when (gridStyle) {
            RadarGridStyle.CIRCULAR -> {
                drawCircularGrid(center, radius, gridLineWidth, gridLineColor)
            }

            RadarGridStyle.POLYGON -> {
                drawPolygonGrid(center, radius, numberOfAxes, gridLineWidth, gridLineColor, startAngle)
            }
        }
    }
}

/**
 * Draw axis lines from center to edges
 */
private fun DrawScope.drawAxisLines(
    center: Offset,
    maxRadius: Float,
    numberOfAxes: Int,
    axisLineWidth: Float,
    axisLineColor: ChartyColor,
    startAngle: Float,
) {
    for (i in 0 until numberOfAxes) {
        val angle = (startAngle + (FULL_CIRCLE_DEGREES * i / numberOfAxes)) * DEGREES_TO_RADIANS
        val endX = center.x + maxRadius * cos(angle)
        val endY = center.y + maxRadius * sin(angle)

        drawLine(
            brush = Brush.linearGradient(axisLineColor.value),
            start = center,
            end = Offset(endX, endY),
            strokeWidth = axisLineWidth,
        )
    }
}

/**
 * Draw a single radar dataset (polygon with fill and points)
 */
private fun DrawScope.drawRadarDataSet(
    center: Offset,
    maxRadius: Float,
    dataSet: RadarDataSet,
    config: RadarChartConfig,
    animationProgress: Float,
) {
    val numberOfAxes = dataSet.axes.size
    val path = Path()
    val points = mutableListOf<Offset>()
    dataSet.axes.fastForEachIndexed { index, axisData ->
        val angle = (config.startAngleDegrees + (FULL_CIRCLE_DEGREES * index / numberOfAxes)) * DEGREES_TO_RADIANS
        val normalizedValue = axisData.getNormalizedValue()
        val radius = maxRadius * normalizedValue * animationProgress

        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)
        val point = Offset(x, y)
        points.add(point)

        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    val dataColor = when (dataSet.color) {
        is ChartyColor.Solid -> dataSet.color.color
        is ChartyColor.Gradient -> dataSet.color.colors.first()
    }

    // Draw filled polygon
    drawPath(
        path = path,
        color = dataColor.copy(alpha = dataSet.fillAlpha * animationProgress),
    )
    drawPath(
        path = path,
        color = dataColor,
        style =
            Stroke(
                width = config.dataLineWidth,
                cap = config.strokeCap,
                join = config.strokeJoin,
            ),
    )
    if (config.showDataPoints) {
        points.forEach { point ->
            drawCircle(
                color = dataColor,
                radius = config.dataPointRadius * animationProgress,
                center = point,
            )
        }
    }
}

/**
 * Draw axis labels
 */
private fun DrawScope.drawAxisLabels(
    center: Offset,
    maxRadius: Float,
    labels: List<String>,
    numberOfAxes: Int,
    config: RadarChartConfig,
    textMeasurer: TextMeasurer,
    startAngle: Float,
) {
    val labelDistance = maxRadius * config.labelConfig.labelDistanceMultiplier
    val textStyle = config.labelConfig.labelTextStyle

    labels.fastForEachIndexed { index, label ->
        val angle = (startAngle + (FULL_CIRCLE_DEGREES * index / numberOfAxes)) * DEGREES_TO_RADIANS
        val x = center.x + labelDistance * cos(angle)
        val y = center.y + labelDistance * sin(angle)
        val textLayoutResult = textMeasurer.measure(
            text = label,
            style = textStyle,
        )
        val textX = x - textLayoutResult.size.width / 2f
        val textY = y - textLayoutResult.size.height / 2f

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(textX, textY),
        )
    }
}
