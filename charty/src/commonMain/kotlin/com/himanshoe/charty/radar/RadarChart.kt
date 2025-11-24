@file:Suppress("LongMethod", "CyclomaticComplexMethod", "MagicNumber", "LongParameterList", "UnusedParameter")

package com.himanshoe.charty.radar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.radar.config.RadarChartConfig
import com.himanshoe.charty.radar.config.RadarGridStyle
import com.himanshoe.charty.radar.data.RadarDataSet
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.PI

private const val FULL_CIRCLE_DEGREES = 360f
private const val DEGREES_TO_RADIANS = PI.toFloat() / 180f

/**
 * Radar Chart (Spider Chart / Web Chart) - Display multivariate data on multiple axes
 *
 * A radar chart displays data as a polygon on axes radiating from a center point.
 * Useful for comparing multiple variables/attributes, showing performance profiles,
 * or displaying capabilities across different dimensions.
 *
 * Usage:
 * ```kotlin
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
 * ```
 *
 * @param data Lambda returning list of datasets to display
 * @param modifier Modifier for the chart
 * @param config Configuration for radar chart appearance
 * @param centerContent Optional composable content for the center
 */
@Composable
fun RadarChart(
    data: () -> List<RadarDataSet>,
    modifier: Modifier = Modifier,
    config: RadarChartConfig = RadarChartConfig(),
    centerContent: @Composable (() -> Unit)? = null
) {
    val dataSets = remember(data) { data() }
    require(dataSets.isNotEmpty()) { "Radar chart data cannot be empty" }

    val numberOfAxes = dataSets.first().axes.size
    require(dataSets.all { it.axes.size == numberOfAxes }) {
        "All datasets must have the same number of axes"
    }

    val animationProgress = remember {
        Animatable(if (config.animation is Animation.Enabled) 0f else 1f)
    }

    LaunchedEffect(config.animation) {
        if (config.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = config.animation.duration)
            )
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val axisLabels = remember(dataSets) { dataSets.first().axes.map { it.label } }

    BoxWithConstraints(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = min(centerX, centerY) * (1f - config.paddingFraction)

            // Draw grid
            if (config.gridConfig.showGridLines) {
                drawRadarGrid(
                    center = Offset(centerX, centerY),
                    maxRadius = maxRadius,
                    numberOfAxes = numberOfAxes,
                    numberOfLevels = config.gridConfig.numberOfGridLevels,
                    gridStyle = config.gridConfig.gridStyle,
                    gridLineWidth = config.gridConfig.gridLineWidth,
                    gridLineColor = config.gridConfig.gridLineColor,
                    startAngle = config.startAngleDegrees
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
                    startAngle = config.startAngleDegrees
                )
            }

            // Draw data sets
            dataSets.fastForEachIndexed { _, dataSet ->
                drawRadarDataSet(
                    center = Offset(centerX, centerY),
                    maxRadius = maxRadius,
                    dataSet = dataSet,
                    config = config,
                    animationProgress = animationProgress.value
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
                    startAngle = config.startAngleDegrees
                )
            }

            // Draw center background
            if (config.centerConfig.centerBackgroundRadius > 0f) {
                drawCircle(
                    color = config.centerConfig.centerBackgroundColor,
                    radius = config.centerConfig.centerBackgroundRadius,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
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
    gridLineColor: Color,
    startAngle: Float
) {
    for (level in 1..numberOfLevels) {
        val radius = (maxRadius * level) / numberOfLevels

        when (gridStyle) {
            RadarGridStyle.CIRCULAR -> {
                drawCircle(
                    color = gridLineColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = gridLineWidth)
                )
            }
            RadarGridStyle.POLYGON -> {
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
                    color = gridLineColor,
                    style = Stroke(width = gridLineWidth)
                )
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
    axisLineColor: Color,
    startAngle: Float
) {
    for (i in 0 until numberOfAxes) {
        val angle = (startAngle + (FULL_CIRCLE_DEGREES * i / numberOfAxes)) * DEGREES_TO_RADIANS
        val endX = center.x + maxRadius * cos(angle)
        val endY = center.y + maxRadius * sin(angle)

        drawLine(
            color = axisLineColor,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = axisLineWidth
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
    animationProgress: Float
) {
    val numberOfAxes = dataSet.axes.size
    val path = Path()
    val points = mutableListOf<Offset>()

    // Calculate all points
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

    // Get color from ChartyColor
    val dataColor = when (dataSet.color) {
        is ChartyColor.Solid -> dataSet.color.color
        is ChartyColor.Gradient -> dataSet.color.colors.first()
    }

    // Draw filled polygon
    drawPath(
        path = path,
        color = dataColor.copy(alpha = dataSet.fillAlpha * animationProgress)
    )

    // Draw outline
    drawPath(
        path = path,
        color = dataColor,
        style = Stroke(
            width = config.dataLineWidth,
            cap = config.strokeCap,
            join = config.strokeJoin
        )
    )

    // Draw data points
    if (config.showDataPoints) {
        points.forEach { point ->
            drawCircle(
                color = dataColor,
                radius = config.dataPointRadius * animationProgress,
                center = point
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
    startAngle: Float
) {
    val labelDistance = maxRadius * config.labelConfig.labelDistanceMultiplier
    val textStyle = config.labelConfig.labelTextStyle

    labels.fastForEachIndexed { index, label ->
        val angle = (startAngle + (FULL_CIRCLE_DEGREES * index / numberOfAxes)) * DEGREES_TO_RADIANS
        val x = center.x + labelDistance * cos(angle)
        val y = center.y + labelDistance * sin(angle)

        val textLayoutResult = textMeasurer.measure(
            text = label,
            style = textStyle
        )

        // Adjust position based on angle to center text properly
        val textX = x - textLayoutResult.size.width / 2f
        val textY = y - textLayoutResult.size.height / 2f

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(textX, textY)
        )
    }
}

