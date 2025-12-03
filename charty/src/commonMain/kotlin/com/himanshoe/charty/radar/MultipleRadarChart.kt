package com.himanshoe.charty.radar

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.animation.rememberChartAnimation
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
private const val LEGEND_PADDING = 8
private const val LEGEND_ITEM_SPACING = 4
private const val LEGEND_ICON_SIZE = 12
private const val LEGEND_ICON_TEXT_SPACING = 6
private const val POINT_INNER_CIRCLE_FRACTION = 0.5f
private const val LABEL_OFFSET_HALF = 2f
private const val ANGLE_QUARTER = PI / 4
private const val ANGLE_THREE_QUARTERS = 3 * PI / 4
private const val ANGLE_MINUS_QUARTER = -PI / 4
private const val ANGLE_MINUS_THREE_QUARTERS = -3 * PI / 4
private const val DEFAULT_ANIMATION_START = 0f
private const val DEFAULT_ANIMATION_END = 1f
private const val MIN_STAGGER_DIVISOR = 0.01f
private const val CLICK_TOLERANCE_MULTIPLIER = 2f

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
        RadarChartWithLegend(
            dataSetsList = dataSetsList,
            numberOfAxes = numberOfAxes,
            config = config,
            modifier = modifier,
            onDataSetClick = onDataSetClick,
        )
    } else {
        RadarChartContent(
            dataSetsList = dataSetsList,
            numberOfAxes = numberOfAxes,
            config = config,
            modifier = modifier,
            onDataSetClick = onDataSetClick,
        )
    }
}

/**
 * Radar chart with legend in different positions
 */
@Composable
private fun RadarChartWithLegend(
    dataSetsList: List<RadarDataSet>,
    numberOfAxes: Int,
    config: MultipleRadarChartConfig,
    modifier: Modifier = Modifier,
    onDataSetClick: ((label: String, index: Int) -> Unit)? = null,
) {
    when (config.legendPosition) {
        LegendPosition.TOP -> {
            CreateVerticalLegendLayout(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = modifier,
                onDataSetClick = onDataSetClick,
                legendFirst = true,
            )
        }

        LegendPosition.BOTTOM -> {
            CreateVerticalLegendLayout(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = modifier,
                onDataSetClick = onDataSetClick,
                legendFirst = false,
            )
        }

        LegendPosition.LEFT -> {
            CreateHorizontalLegendLayout(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = modifier,
                onDataSetClick = onDataSetClick,
                legendFirst = true,
            )
        }

        LegendPosition.RIGHT -> {
            CreateHorizontalLegendLayout(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = modifier,
                onDataSetClick = onDataSetClick,
                legendFirst = false,
            )
        }

        LegendPosition.TOP_LEFT -> {
            CreateOverlayLegendLayout(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = modifier,
                alignment = Alignment.TopStart,
                onDataSetClick = onDataSetClick,
            )
        }

        LegendPosition.TOP_RIGHT -> {
            CreateOverlayLegendLayout(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = modifier,
                alignment = Alignment.TopEnd,
                onDataSetClick = onDataSetClick,
            )
        }

        LegendPosition.BOTTOM_LEFT -> {
            CreateOverlayLegendLayout(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = modifier,
                alignment = Alignment.BottomStart,
                onDataSetClick = onDataSetClick,
            )
        }

        LegendPosition.BOTTOM_RIGHT -> {
            CreateOverlayLegendLayout(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = modifier,
                alignment = Alignment.BottomEnd,
                onDataSetClick = onDataSetClick,
            )
        }
    }
}

/**
 * Creates a vertical layout with legend (top or bottom)
 */
@Composable
private fun CreateVerticalLegendLayout(
    dataSetsList: List<RadarDataSet>,
    numberOfAxes: Int,
    config: MultipleRadarChartConfig,
    onDataSetClick: ((label: String, index: Int) -> Unit)?,
    legendFirst: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (legendFirst) {
            Legend(
                dataSetsList,
                Modifier.padding(bottom = LEGEND_PADDING.dp),
                config.legendTextStyle,
                onDataSetClick,
            )
            RadarChartContent(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = Modifier.weight(1f),
                onDataSetClick = onDataSetClick,
            )
        } else {
            RadarChartContent(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = Modifier.weight(1f),
                onDataSetClick = onDataSetClick,
            )
            Legend(
                dataSetsList,
                Modifier.padding(top = LEGEND_PADDING.dp),
                config.legendTextStyle,
                onDataSetClick,
            )
        }
    }
}

/**
 * Creates a horizontal layout with legend (left or right)
 */
@Composable
private fun CreateHorizontalLegendLayout(
    dataSetsList: List<RadarDataSet>,
    numberOfAxes: Int,
    config: MultipleRadarChartConfig,
    onDataSetClick: ((label: String, index: Int) -> Unit)?,
    legendFirst: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        if (legendFirst) {
            Legend(
                dataSetsList,
                Modifier.padding(end = LEGEND_PADDING.dp),
                config.legendTextStyle,
                onDataSetClick,
            )
            RadarChartContent(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = Modifier.weight(1f),
                onDataSetClick = onDataSetClick,
            )
        } else {
            RadarChartContent(
                dataSetsList = dataSetsList,
                numberOfAxes = numberOfAxes,
                config = config,
                modifier = Modifier.weight(1f),
                onDataSetClick = onDataSetClick,
            )
            Legend(
                dataSetsList,
                Modifier.padding(start = LEGEND_PADDING.dp),
                config.legendTextStyle,
                onDataSetClick,
            )
        }
    }
}

/**
 * Creates a Box layout with legend overlaid on the chart
 */
@Composable
private fun CreateOverlayLegendLayout(
    dataSetsList: List<RadarDataSet>,
    numberOfAxes: Int,
    config: MultipleRadarChartConfig,
    alignment: Alignment,
    onDataSetClick: ((label: String, index: Int) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        RadarChartContent(
            dataSetsList = dataSetsList,
            numberOfAxes = numberOfAxes,
            config = config,
            modifier = Modifier.fillMaxSize(),
            onDataSetClick = onDataSetClick,
        )
        Legend(
            dataSetsList,
            Modifier
                .align(alignment)
                .padding(LEGEND_PADDING.dp),
            config.legendTextStyle,
            onDataSetClick,
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
    legendTextStyle: TextStyle = TextStyle(fontSize = LEGEND_ICON_SIZE.sp),
    onDataSetClick: ((label: String, index: Int) -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LEGEND_ITEM_SPACING.dp),
    ) {
        dataSets.forEachIndexed { index, dataSet ->
            val dataColor =
                when (dataSet.color) {
                    is ChartyColor.Solid -> dataSet.color.color
                    is ChartyColor.Gradient -> dataSet.color.colors.first()
                }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LEGEND_ICON_TEXT_SPACING.dp),
                modifier =
                    if (onDataSetClick != null) {
                        Modifier.clickable { onDataSetClick(dataSet.label, index) }
                    } else {
                        Modifier
                    },
            ) {
                Surface(
                    modifier = Modifier.size(LEGEND_ICON_SIZE.dp),
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
    onDataSetClick: ((label: String, index: Int) -> Unit)? = null,
) {
    val animationProgress = rememberRadarAnimation(config.radarConfig.animation)
    val textMeasurer = rememberTextMeasurer()
    val axisLabels = remember(dataSetsList) { dataSetsList.first().axes.map { it.label } }
    val dataPointPositions = remember { mutableMapOf<Int, List<Offset>>() }

    BoxWithConstraints(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (onDataSetClick != null) {
                        Modifier.pointerInput(dataSetsList, dataPointPositions) {
                            detectTapGestures { offset ->
                                handleDataPointClick(offset, dataPointPositions, dataSetsList, config, onDataSetClick)
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = min(size.width / 2f, size.height / 2f) * (1f - config.radarConfig.paddingFraction)

            // Draw grid
            if (config.radarConfig.gridConfig.showGridLines) {
                drawRadarGrid(
                    center = center,
                    maxRadius = maxRadius,
                    numberOfAxes = numberOfAxes,
                    numberOfLevels = config.radarConfig.gridConfig.numberOfGridLevels,
                    gridStyle = config.radarConfig.gridConfig.gridStyle,
                    gridLineWidth = config.radarConfig.gridConfig.gridLineWidth,
                    gridLineColor = config.radarConfig.gridConfig.gridLineColor,
                    startAngle = config.radarConfig.startAngleDegrees,
                )
            }
            if (config.radarConfig.gridConfig.showAxisLines) {
                drawAxisLines(
                    center = center,
                    maxRadius = maxRadius,
                    numberOfAxes = numberOfAxes,
                    axisLineWidth = config.radarConfig.gridConfig.axisLineWidth,
                    axisLineColor = config.radarConfig.gridConfig.axisLineColor,
                    startAngle = config.radarConfig.startAngleDegrees,
                )
            }
            dataSetsList.fastForEachIndexed { index, dataSet ->
                val datasetAnimationProgress = calculateDatasetAnimationProgress(
                    index = index,
                    animationProgress = animationProgress.value,
                    config = config,
                )

                val points = drawRadarDataSet(
                    center = center,
                    maxRadius = maxRadius,
                    dataSet = dataSet,
                    config = config,
                    animationProgress = datasetAnimationProgress,
                )

                if (onDataSetClick != null) {
                    dataPointPositions[index] = points
                }
            }

            // Draw labels
            if (config.radarConfig.labelConfig.showLabels) {
                drawAxisLabels(
                    center = center,
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
                    center = center,
                )
            }
        }
    }
}

/**
 * Remember and manage radar chart animation
 */
@Composable
private fun rememberRadarAnimation(animation: Animation): Animatable<Float, *> {
    return rememberChartAnimation(
        animation = animation,
        initialValue = if (animation is Animation.Enabled) DEFAULT_ANIMATION_START else DEFAULT_ANIMATION_END,
        targetValue = DEFAULT_ANIMATION_END
    )
}

/**
 * Calculate animation progress for a specific dataset (with optional stagger)
 */
private fun calculateDatasetAnimationProgress(
    index: Int,
    animationProgress: Float,
    config: MultipleRadarChartConfig,
): Float {
    return if (config.staggerAnimation) {
        val delay = index * config.staggerDelay
        val adjustedProgress = (animationProgress - delay).coerceIn(DEFAULT_ANIMATION_START, DEFAULT_ANIMATION_END)
        (adjustedProgress / (DEFAULT_ANIMATION_END - delay).coerceAtLeast(MIN_STAGGER_DIVISOR))
            .coerceIn(DEFAULT_ANIMATION_START, DEFAULT_ANIMATION_END)
    } else {
        animationProgress
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
        drawGridLevel(
            center = center,
            radius = radius,
            numberOfAxes = numberOfAxes,
            gridStyle = gridStyle,
            gridLineWidth = gridLineWidth,
            gridLineColor = gridLineColor,
            startAngle = startAngle,
        )
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
 * Returns the list of data point positions for click detection
 */
private fun DrawScope.drawRadarDataSet(
    center: Offset,
    maxRadius: Float,
    dataSet: RadarDataSet,
    config: MultipleRadarChartConfig,
    animationProgress: Float,
): List<Offset> {
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
                    radius = (pointRadius * POINT_INNER_CIRCLE_FRACTION) * animationProgress,
                    center = point,
                )
            }
        }
    }

    return points
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

        val isBottom = angle > ANGLE_QUARTER && angle < ANGLE_THREE_QUARTERS
        val isTop = angle > ANGLE_MINUS_THREE_QUARTERS && angle < ANGLE_MINUS_QUARTER
        val isRight = angle >= ANGLE_MINUS_QUARTER && angle <= ANGLE_QUARTER

        val offsetX =
            when {
                isBottom || isTop -> -textWidth / LABEL_OFFSET_HALF
                isRight -> 0f
                else -> -textWidth.toFloat()
            }

        val offsetY =
            when {
                isBottom -> 0f
                isTop -> -textHeight.toFloat()
                else -> -textHeight / LABEL_OFFSET_HALF
            }

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(x + offsetX, y + offsetY),
        )
    }
}

/**
 * Handle clicks on data points in the radar chart
 */
private fun handleDataPointClick(
    clickOffset: Offset,
    dataPointPositions: Map<Int, List<Offset>>,
    dataSetsList: List<RadarDataSet>,
    config: MultipleRadarChartConfig,
    onDataSetClick: (label: String, index: Int) -> Unit,
) {
    val clickTolerance = (config.datasetPointRadius ?: config.radarConfig.dataPointRadius) * CLICK_TOLERANCE_MULTIPLIER

    // Iterate through datasets in reverse order (top to bottom in drawing)
    // to prioritize clicking on the topmost dataset
    for (datasetIndex in dataSetsList.indices.reversed()) {
        val points = dataPointPositions[datasetIndex] ?: continue

        // Check if any point in this dataset was clicked
        for (pointIndex in points.indices) {
            val point = points[pointIndex]
            val distance = kotlin.math.sqrt(
                (clickOffset.x - point.x) * (clickOffset.x - point.x) +
                    (clickOffset.y - point.y) * (clickOffset.y - point.y),
            )

            if (distance <= clickTolerance) {
                onDataSetClick(dataSetsList[datasetIndex].label, pointIndex)
                return
            }
        }
    }
}

