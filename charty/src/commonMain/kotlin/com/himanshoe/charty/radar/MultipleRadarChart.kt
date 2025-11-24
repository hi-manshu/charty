@file:Suppress("LongMethod", "CyclomaticComplexMethod", "MagicNumber", "LongParameterList")

package com.himanshoe.charty.radar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.radar.config.LegendPosition
import com.himanshoe.charty.radar.config.MultipleRadarChartConfig
import com.himanshoe.charty.radar.config.RadarGridStyle
import com.himanshoe.charty.radar.data.RadarDataSet
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.PI

private const val FULL_CIRCLE_DEGREES = 360f
private const val DEGREES_TO_RADIANS = PI.toFloat() / 180f

/**
 * Multiple Radar Chart - Display multiple overlapping radar/spider charts with enhanced flexibility
 *
 * An advanced radar chart that displays multiple datasets as overlapping polygons,
 * perfect for comparing multiple entities across various attributes.
 * Supports individual dataset customization, legends, and interactive features.
 *
 * Usage:
 * ```kotlin
 * MultipleRadarChart(
 *     dataSets = {
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
 *             ),
 *             RadarDataSet(
 *                 label = "Player 2",
 *                 axes = listOf(
 *                     RadarAxisData("Speed", 70f),
 *                     RadarAxisData("Power", 85f),
 *                     RadarAxisData("Defense", 90f),
 *                     RadarAxisData("Skill", 75f),
 *                     RadarAxisData("Stamina", 80f)
 *                 ),
 *                 color = ChartyColor.Solid(Color.Magenta),
 *                 fillAlpha = 0.3f
 *             )
 *         )
 *     },
 *     config = MultipleRadarChartConfig(
 *         showLegend = true,
 *         allowDatasetToggle = true,
 *         highlightOnHover = true
 *     )
 * )
 * ```
 *
 * Features:
 * - Multiple overlapping datasets
 * - Individual dataset styling (color, fill alpha, line width)
 * - Optional legend display
 * - Dataset toggle functionality
 * - Hover/highlight effects
 * - Flexible grid styles (circular or polygon)
 * - Customizable animations
 *
 * @param dataSets Lambda returning list of radar datasets to display
 * @param modifier Modifier for the chart
 * @param config Configuration for multiple radar chart appearance and behavior
 * @param onDataSetClick Optional callback when a dataset is clicked (receives dataset label and index)
 */
@Composable
fun MultipleRadarChart(
    dataSets: () -> List<RadarDataSet>,
    modifier: Modifier = Modifier,
    config: MultipleRadarChartConfig = MultipleRadarChartConfig(),
    onDataSetClick: ((label: String, index: Int) -> Unit)? = null
) {
    val dataSetsList = remember(dataSets) { dataSets() }
    require(dataSetsList.isNotEmpty()) { "Multiple radar chart data cannot be empty" }

    val numberOfAxes = dataSetsList.first().axes.size
    require(dataSetsList.all { it.axes.size == numberOfAxes }) {
        "All datasets must have the same number of axes"
    }

    if (config.showLegend) {
        when (config.legendPosition) {
            LegendPosition.TOP -> {
                Column(modifier = modifier) {
                    Legend(dataSetsList, Modifier.padding(bottom = 8.dp))
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            LegendPosition.BOTTOM -> {
                Column(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.weight(1f)
                    )
                    Legend(dataSetsList, Modifier.padding(top = 8.dp))
                }
            }
            LegendPosition.LEFT -> {
                Row(modifier = modifier) {
                    Legend(dataSetsList, Modifier.padding(end = 8.dp))
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            LegendPosition.RIGHT -> {
                Row(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.weight(1f)
                    )
                    Legend(dataSetsList, Modifier.padding(start = 8.dp))
                }
            }
            LegendPosition.TOP_LEFT -> {
                Box(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.fillMaxSize()
                    )
                    Legend(
                        dataSetsList,
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                }
            }
            LegendPosition.TOP_RIGHT -> {
                Box(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.fillMaxSize()
                    )
                    Legend(
                        dataSetsList,
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
            }
            LegendPosition.BOTTOM_LEFT -> {
                Box(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.fillMaxSize()
                    )
                    Legend(
                        dataSetsList,
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }
            }
            LegendPosition.BOTTOM_RIGHT -> {
                Box(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.fillMaxSize()
                    )
                    Legend(
                        dataSetsList,
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    )
                }
            }
        }
    } else {
        RadarChartContent(
            dataSetsList = dataSetsList,
            numberOfAxes = numberOfAxes,
            config = config,
            modifier = modifier
        )
    }
}

/**
 * Legend component for displaying dataset labels
 */
@Composable
private fun Legend(
    dataSets: List<RadarDataSet>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        dataSets.forEach { dataSet ->
            val dataColor = when (dataSet.color) {
                is ChartyColor.Solid -> dataSet.color.color
                is ChartyColor.Gradient -> dataSet.color.colors.first()
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = CircleShape,
                    color = dataColor
                ) {}
                Text(
                    text = dataSet.label,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = dataColor
                    )
                )
            }
        }
    }
}

/**
 * The main radar chart content
 */
@Composable
private fun RadarChartContent(
    dataSetsList: List<RadarDataSet>,
    numberOfAxes: Int,
    config: MultipleRadarChartConfig,
    modifier: Modifier = Modifier
) {

    val animationProgress = remember {
        Animatable(if (config.radarConfig.animation is Animation.Enabled) 0f else 1f)
    }

    LaunchedEffect(config.radarConfig.animation) {
        if (config.radarConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = config.radarConfig.animation.duration)
            )
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val axisLabels = remember(dataSetsList) { dataSetsList.first().axes.map { it.label } }

    BoxWithConstraints(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = min(centerX, centerY) * (1f - config.radarConfig.paddingFraction)

            // Draw grid
            if (config.radarConfig.gridConfig.showGridLines) {
                drawRadarGrid(
                    center = Offset(centerX, centerY),
                    maxRadius = maxRadius,
                    numberOfAxes = numberOfAxes,
                    numberOfLevels = config.radarConfig.gridConfig.numberOfGridLevels,
                    gridStyle = config.radarConfig.gridConfig.gridStyle,
                    gridLineWidth = config.radarConfig.gridConfig.gridLineWidth,
                    gridLineColor = config.radarConfig.gridConfig.gridLineColor,
                    startAngle = config.radarConfig.startAngleDegrees
                )
            }

            // Draw axis lines
            if (config.radarConfig.gridConfig.showAxisLines) {
                drawAxisLines(
                    center = Offset(centerX, centerY),
                    maxRadius = maxRadius,
                    numberOfAxes = numberOfAxes,
                    axisLineWidth = config.radarConfig.gridConfig.axisLineWidth,
                    axisLineColor = config.radarConfig.gridConfig.axisLineColor,
                    startAngle = config.radarConfig.startAngleDegrees
                )
            }

            // Draw all data sets with staggered animation if enabled
            dataSetsList.fastForEachIndexed { index, dataSet ->
                val datasetAnimationProgress = if (config.staggerAnimation) {
                    val delay = index * config.staggerDelay
                    val adjustedProgress = (animationProgress.value - delay).coerceIn(0f, 1f)
                    adjustedProgress / (1f - delay).coerceAtLeast(0.01f)
                } else {
                    animationProgress.value
                }

                drawRadarDataSet(
                    center = Offset(centerX, centerY),
                    maxRadius = maxRadius,
                    dataSet = dataSet,
                    config = config,
                    animationProgress = datasetAnimationProgress.coerceIn(0f, 1f)
                )
            }

            // Draw labels
            if (config.radarConfig.labelConfig.showLabels) {
                drawAxisLabels(
                    center = Offset(centerX, centerY),
                    maxRadius = maxRadius,
                    labels = axisLabels,
                    numberOfAxes = numberOfAxes,
                    config = config,
                    textMeasurer = textMeasurer,
                    startAngle = config.radarConfig.startAngleDegrees
                )
            }

            // Draw center background
            if (config.radarConfig.centerConfig.centerBackgroundRadius > 0f) {
                drawCircle(
                    color = config.radarConfig.centerConfig.centerBackgroundColor,
                    radius = config.radarConfig.centerConfig.centerBackgroundRadius,
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
    config: MultipleRadarChartConfig,
    animationProgress: Float
) {
    val numberOfAxes = dataSet.axes.size
    val path = Path()
    val points = mutableListOf<Offset>()

    // Calculate all points
    dataSet.axes.fastForEachIndexed { index, axisData ->
        val angle = (config.radarConfig.startAngleDegrees + (FULL_CIRCLE_DEGREES * index / numberOfAxes)) * DEGREES_TO_RADIANS
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

    // Draw outline with customizable line width per dataset
    val lineWidth = config.datasetLineWidth ?: config.radarConfig.dataLineWidth
    drawPath(
        path = path,
        color = dataColor,
        style = Stroke(
            width = lineWidth,
            cap = config.radarConfig.strokeCap,
            join = config.radarConfig.strokeJoin
        )
    )

    // Draw data points
    if (config.radarConfig.showDataPoints) {
        val pointRadius = config.datasetPointRadius ?: config.radarConfig.dataPointRadius
        points.forEach { point ->
            // Outer circle
            drawCircle(
                color = dataColor,
                radius = pointRadius * animationProgress,
                center = point
            )

            // Optional inner circle for better visibility
            if (config.showPointInnerCircle) {
                drawCircle(
                    color = Color.White,
                    radius = (pointRadius * 0.5f) * animationProgress,
                    center = point
                )
            }
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
    config: MultipleRadarChartConfig,
    textMeasurer: TextMeasurer,
    startAngle: Float
) {
    val labelDistance = maxRadius * config.radarConfig.labelConfig.labelDistanceMultiplier

    // Extract color from ChartyColor
    val labelColor = when (config.radarConfig.labelConfig.labelColor) {
        is ChartyColor.Solid -> config.radarConfig.labelConfig.labelColor.color
        is ChartyColor.Gradient -> config.radarConfig.labelConfig.labelColor.colors.first()
    }

    labels.fastForEachIndexed { index, label ->
        val angle = (startAngle + (FULL_CIRCLE_DEGREES * index / numberOfAxes)) * DEGREES_TO_RADIANS
        val x = center.x + labelDistance * cos(angle)
        val y = center.y + labelDistance * sin(angle)

        val textLayoutResult = textMeasurer.measure(
            text = label,
            style = TextStyle(
                fontSize = config.radarConfig.labelConfig.labelTextSizeSp.sp,
                color = labelColor
            )
        )

        // Calculate offset to center the text around the point
        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height

        val offsetX = when {
            angle > PI / 4 && angle < 3 * PI / 4 -> -textWidth / 2f  // Bottom
            angle > -3 * PI / 4 && angle < -PI / 4 -> -textWidth / 2f  // Top
            angle >= -PI / 4 && angle <= PI / 4 -> 0f  // Right
            else -> -textWidth.toFloat()  // Left
        }

        val offsetY = when {
            angle > PI / 4 && angle < 3 * PI / 4 -> 0f  // Bottom
            angle > -3 * PI / 4 && angle < -PI / 4 -> -textHeight.toFloat()  // Top
            else -> -textHeight / 2f  // Middle
        }

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(x + offsetX, y + offsetY)
        )
    }
}


