@file:Suppress(
    "LongMethod",
    "CyclomaticComplexMethod",
    "MagicNumber",
    "LongParameterList",
    "UnusedParameter",
    "NestedBlockDepth",
    "FunctionNaming",
)

package com.himanshoe.charty.radar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val FULL_CIRCLE_DEGREES = 360f
private const val DEGREES_TO_RADIANS = PI.toFloat() / 180f

/**
 * Multiple Radar Chart that displays overlapping datasets with optional legend.
 *
 * @param dataSets Lambda providing datasets to render
 * @param modifier Modifier for the composable root
 * @param config Visual and behavioral configuration
 * @param onDataSetClick Optional callback when a dataset entry is clicked
 */
@Composable
fun MultipleRadarChart(
    dataSets: () -> List<RadarDataSet>,
    modifier: Modifier = Modifier,
    config: MultipleRadarChartConfig = MultipleRadarChartConfig(),
    onDataSetClick: ((label: String, index: Int) -> Unit)? = null,
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
                    Legend(dataSetsList, Modifier.padding(bottom = 8.dp), config.legendTextStyle)
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            LegendPosition.BOTTOM -> {
                Column(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.weight(1f),
                    )
                    Legend(dataSetsList, Modifier.padding(top = 8.dp), config.legendTextStyle)
                }
            }

            LegendPosition.LEFT -> {
                Row(modifier = modifier) {
                    Legend(dataSetsList, Modifier.padding(end = 8.dp), config.legendTextStyle)
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            LegendPosition.RIGHT -> {
                Row(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.weight(1f),
                    )
                    Legend(dataSetsList, Modifier.padding(start = 8.dp), config.legendTextStyle)
                }
            }

            LegendPosition.TOP_LEFT -> {
                Box(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Legend(
                        dataSetsList,
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        config.legendTextStyle,
                    )
                }
            }

            LegendPosition.TOP_RIGHT -> {
                Box(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Legend(
                        dataSetsList,
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        config.legendTextStyle,
                    )
                }
            }

            LegendPosition.BOTTOM_LEFT -> {
                Box(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Legend(
                        dataSetsList,
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        config.legendTextStyle,
                    )
                }
            }

            LegendPosition.BOTTOM_RIGHT -> {
                Box(modifier = modifier) {
                    RadarChartContent(
                        dataSetsList = dataSetsList,
                        numberOfAxes = numberOfAxes,
                        config = config,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Legend(
                        dataSetsList,
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        config.legendTextStyle,
                    )
                }
            }
        }
    } else {
        RadarChartContent(
            dataSetsList = dataSetsList,
            numberOfAxes = numberOfAxes,
            config = config,
            modifier = modifier,
        )
    }
}

/**
 * Legend component for displaying dataset labels
 */
@Composable
private fun Legend(
    dataSets: List<RadarDataSet>,
    modifier: Modifier = Modifier,
    legendTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    onDataSetClick: ((label: String, index: Int) -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        dataSets.forEachIndexed { index, dataSet ->
            val dataColor =
                when (dataSet.color) {
                    is ChartyColor.Solid -> dataSet.color.color
                    is ChartyColor.Gradient -> dataSet.color.colors.first()
                }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier =
                    if (onDataSetClick != null) {
                        Modifier.clickable { onDataSetClick(dataSet.label, index) }
                    } else {
                        Modifier
                    },
            ) {
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = CircleShape,
                    color = dataColor,
                ) {}
                Text(
                    text = dataSet.label,
                    style =
                        legendTextStyle.copy(
                            color =
                                if (legendTextStyle.color == Color.Unspecified) {
                                    dataColor
                                } else {
                                    legendTextStyle.color
                                },
                        ),
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
    modifier: Modifier = Modifier,
) {
    val animationProgress =
        remember {
            Animatable(if (config.radarConfig.animation is Animation.Enabled) 0f else 1f)
        }

    LaunchedEffect(config.radarConfig.animation) {
        if (config.radarConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = config.radarConfig.animation.duration),
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
                    startAngle = config.radarConfig.startAngleDegrees,
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
                    startAngle = config.radarConfig.startAngleDegrees,
                )
            }

            // Draw all data sets with staggered animation if enabled
            dataSetsList.fastForEachIndexed { index, dataSet ->
                val datasetAnimationProgress =
                    if (config.staggerAnimation) {
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
                    animationProgress = datasetAnimationProgress.coerceIn(0f, 1f),
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
                    startAngle = config.radarConfig.startAngleDegrees,
                )
            }

            // Draw center background
            if (config.radarConfig.centerConfig.centerBackgroundRadius > 0f) {
                drawCircle(
                    color = config.radarConfig.centerConfig.centerBackgroundColor,
                    radius = config.radarConfig.centerConfig.centerBackgroundRadius,
                    center = Offset(centerX, centerY),
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
    gridLineColor: ChartyColor,
    startAngle: Float,
) {
    for (level in 1..numberOfLevels) {
        val radius = (maxRadius * level) / numberOfLevels

        when (gridStyle) {
            RadarGridStyle.CIRCULAR -> {
                drawCircle(
                    brush = Brush.linearGradient(gridLineColor.value),
                    radius = radius,
                    center = center,
                    style = Stroke(width = gridLineWidth),
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
                    brush = Brush.linearGradient(gridLineColor.value),
                    style = Stroke(width = gridLineWidth),
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
    config: MultipleRadarChartConfig,
    animationProgress: Float,
) {
    val numberOfAxes = dataSet.axes.size
    val path = Path()
    val points = mutableListOf<Offset>()

    // Calculate all points
    dataSet.axes.fastForEachIndexed { index, axisData ->
        val baseAngle = config.radarConfig.startAngleDegrees
        val sweepPerAxis = FULL_CIRCLE_DEGREES * index / numberOfAxes
        val angle = (baseAngle + sweepPerAxis) * DEGREES_TO_RADIANS
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
    val dataColor =
        when (dataSet.color) {
            is ChartyColor.Solid -> dataSet.color.color
            is ChartyColor.Gradient -> dataSet.color.colors.first()
        }

    // Draw filled polygon
    drawPath(
        path = path,
        color = dataColor.copy(alpha = dataSet.fillAlpha * animationProgress),
    )

    // Draw outline with customizable line width per dataset
    val lineWidth = config.datasetLineWidth ?: config.radarConfig.dataLineWidth
    drawPath(
        path = path,
        color = dataColor,
        style =
            Stroke(
                width = lineWidth,
                cap = config.radarConfig.strokeCap,
                join = config.radarConfig.strokeJoin,
            ),
    )

    // Draw data points
    if (config.radarConfig.showDataPoints) {
        val pointRadius = config.datasetPointRadius ?: config.radarConfig.dataPointRadius
        points.forEach { point ->
            // Outer circle
            drawCircle(
                color = dataColor,
                radius = pointRadius * animationProgress,
                center = point,
            )

            // Optional inner circle for better visibility
            if (config.showPointInnerCircle) {
                drawCircle(
                    color = Color.White,
                    radius = (pointRadius * 0.5f) * animationProgress,
                    center = point,
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
    startAngle: Float,
) {
    val labelDistance = maxRadius * config.radarConfig.labelConfig.labelDistanceMultiplier
    val textStyle = config.radarConfig.labelConfig.labelTextStyle

    labels.fastForEachIndexed { index, label ->
        val angle = (startAngle + (FULL_CIRCLE_DEGREES * index / numberOfAxes)) * DEGREES_TO_RADIANS
        val x = center.x + labelDistance * cos(angle)
        val y = center.y + labelDistance * sin(angle)

        val textLayoutResult =
            textMeasurer.measure(
                text = label,
                style = textStyle,
            )

        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height

        val isBottom = angle > PI / 4 && angle < 3 * PI / 4
        val isTop = angle > -3 * PI / 4 && angle < -PI / 4
        val isRight = angle >= -PI / 4 && angle <= PI / 4

        val offsetX =
            when {
                isBottom || isTop -> -textWidth / 2f
                isRight -> 0f
                else -> -textWidth.toFloat()
            }

        val offsetY =
            when {
                isBottom -> 0f
                isTop -> -textHeight.toFloat()
                else -> -textHeight / 2f
            }

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(x + offsetX, y + offsetY),
        )
    }
}
