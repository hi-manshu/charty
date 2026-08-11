package com.himanshoe.charty.pie

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.accessibility.generatePieChartDescription
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.pie.config.PieChartConfig
import com.himanshoe.charty.pie.config.PieChartStyle
import com.himanshoe.charty.pie.data.PieData
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

private const val CHART_SIZE_MULTIPLIER = 0.8f
private const val PIE_LABEL_RADIUS_MULTIPLIER = 0.65f
private const val DONUT_LABEL_RADIUS_DIVISOR = 2f
private const val LABEL_ANIMATION_THRESHOLD = 0.5f
private const val PERCENTAGE_PRECISION_MULTIPLIER = 10.0
private const val FULL_CIRCLE_DEGREES = 360f
private const val HALF_DIVIDER = 2f
private const val DOUBLE_MULTIPLIER = 2f
private const val DEGREES_TO_RADIANS = PI / 180.0
private const val RADIANS_TO_DEGREES = 180.0 / PI

/**
 * Parameters for drawing pie chart content
 */
private data class PieChartContentParams(
    val dataList: List<PieData>,
    val sliceColors: List<ChartyColor>,
    val total: Float,
    val config: PieChartConfig,
    val animationProgress: Animatable<Float, *>,
    val selectedSliceIndex: Int?,
    val selectedScale: Animatable<Float, *>,
    val centerContent: @Composable (() -> Unit)?,
    val onSliceClick: (Int) -> Unit,
)

/**
 * Parameters for drawing pie slices
 */
private data class PieSliceDrawParams(
    val dataList: List<PieData>,
    val colors: List<ChartyColor>,
    val total: Float,
    val center: Offset,
    val radius: Float,
    val config: PieChartConfig,
    val animationProgress: Float,
    val selectedSliceIndex: Int?,
    val selectedScale: Float,
    val textMeasurer: androidx.compose.ui.text.TextMeasurer,
)

/**
 * Pie/Donut Chart - Display data as circular slices with comprehensive features
 *
 * A highly configurable pie or donut chart that supports:
 * - Pie and Donut chart styles
 * - Click interactions with callbacks
 * - Smooth animations
 * - Customizable legends (top, bottom, left, right)
 * - Slice labels with percentages/values
 * - Selected slice highlighting
 * - Center text for donut charts
 * - Optimized rendering for performance
 *
 * Performance Features:
 * - Efficient path caching
 * - Optimized touch detection
 * - Smart recomposition scoping
 * - Memory-efficient color management
 *
 * Usage:
 * ```kotlin
 * // Basic Pie Chart
 * PieChart(
 *     data = {
 *         listOf(
 *             PieData(label = "Product A", value = 45f),
 *             PieData(label = "Product B", value = 30f),
 *             PieData(label = "Product C", value = 25f)
 *         )
 *     },
 *     color = ChartyColor.Gradient(
 *         listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800))
 *     )
 * )
 *
 * // Interactive Donut Chart with Click Listener
 * PieChart(
 *     data = { pieDataList },
 *     modifier = Modifier.fillMaxWidth().height(400.dp),
 *     color = ChartyColor.Gradient(colorList),
 *     config = PieChartConfig(
 *         style = PieChartStyle.DONUT,
 *         donutHoleRatio = 0.6f,
 *         legendConfig = LegendConfig(
 *             position = LegendPosition.RIGHT,
 *             showPercentage = true
 *         ),
 *         animation = Animation.Enabled(duration = 1000),
 *         interactionConfig = InteractionConfig(
 *             selectedScale = 1.1f,
 *             selectedOffset = 10f
 *         )
 *     ),
 *     onSliceClick = { slice, index ->
 *         println("Clicked: ${slice.label} (${slice.value})")
 *     }
 * )
 * ```
 *
 * @param data Lambda returning list of pie slice data
 * @param modifier Modifier for the chart
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param color Color configuration - Solid or Gradient for slice colors
 * @param config Comprehensive configuration for chart appearance and behavior
 * @param onSliceClick Callback invoked when a slice is clicked (receives PieData and index)
 * @param centerContent Optional composable content for donut chart center
 * @param accessibilityDescription Overrides the auto-generated screen-reader description. Pass an empty string to suppress it.
 */
@Composable
fun PieChart(
    data: () -> List<PieData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    config: PieChartConfig = PieChartConfig(),
    onSliceClick: ((PieData, Int) -> Unit)? = null,
    centerContent: @Composable (() -> Unit)? = null,
    accessibilityDescription: String? = null,
) {
    val dataList = remember(data) { data() }
    if (dataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val chartDescription =
        remember(dataList, config.style, accessibilityDescription) {
            when (accessibilityDescription) {
                "" -> null
                null ->
                    generatePieChartDescription(
                        data = dataList,
                        chartTypeName =
                            if (config.style == PieChartStyle.DONUT) {
                                "Donut"
                            } else {
                                "Pie"
                            },
                    )
                else -> accessibilityDescription
            }
        }
    val semanticsModifier =
        if (chartDescription != null) {
            Modifier.semantics { contentDescription = chartDescription }
        } else {
            Modifier
        }
    val total = remember(dataList) { dataList.sumOf { it.value.toDouble() }.toFloat() }
    require(total > 0f) { "Total of all pie slices must be positive" }
    val sliceColors =
        remember(dataList, color) {
            generateSliceColors(dataList = dataList, color = color)
        }
    val animationProgress = rememberChartAnimation(config.animation)
    var selectedSliceIndex by remember { mutableStateOf<Int?>(null) }
    val selectedScale = remember { Animatable(1f) }
    LaunchedEffect(selectedSliceIndex) {
        if (selectedSliceIndex != null && config.interactionConfig.isEnabled) {
            selectedScale.animateTo(
                targetValue = config.interactionConfig.selectedScaleMultiplier,
                animationSpec = tween(durationMillis = config.interactionConfig.selectionAnimationDurationMs),
            )
        } else {
            selectedScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = config.interactionConfig.selectionAnimationDurationMs),
            )
        }
    }
    PieChartContent(
        params =
            PieChartContentParams(
                dataList = dataList,
                sliceColors = sliceColors,
                total = total,
                config = config,
                animationProgress = animationProgress,
                selectedSliceIndex = selectedSliceIndex,
                selectedScale = selectedScale,
                centerContent = centerContent,
                onSliceClick =
                    onSliceClickLambda(dataList) { index ->
                        selectedSliceIndex =
                            if (selectedSliceIndex == index) {
                                null
                            } else {
                                index
                            }
                        onSliceClick?.invoke(dataList[index], index)
                    },
            ),
        modifier = modifier.then(semanticsModifier),
    )
}

private fun onSliceClickLambda(
    dataList: List<PieData>,
    handler: (Int) -> Unit,
): (Int) -> Unit =
    { index ->
        require(index in dataList.indices) { "Invalid slice index: $index" }
        handler(index)
    }

/**
 * Main pie chart drawing component
 */
@Composable
private fun PieChartContent(
    params: PieChartContentParams,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(params.dataList, params.config.interactionConfig.isEnabled) {
                        if (params.config.interactionConfig.isEnabled) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / HALF_DIVIDER, size.height / HALF_DIVIDER)
                                val radius = minOf(size.width, size.height) / HALF_DIVIDER * CHART_SIZE_MULTIPLIER

                                val clickedIndex =
                                    findClickedSlice(
                                        touchPosition = offset,
                                        center = center,
                                        radius = radius,
                                        dataList = params.dataList,
                                        total = params.total,
                                        config = params.config,
                                    )

                                clickedIndex?.let { params.onSliceClick(it) }
                            }
                        }
                    },
        ) {
            val center = Offset(size.width / HALF_DIVIDER, size.height / HALF_DIVIDER)
            val radius = minOf(size.width, size.height) / HALF_DIVIDER * CHART_SIZE_MULTIPLIER

            drawPieSlices(
                params =
                    PieSliceDrawParams(
                        dataList = params.dataList,
                        colors = params.sliceColors,
                        total = params.total,
                        center = center,
                        radius = radius,
                        config = params.config,
                        animationProgress = params.animationProgress.value,
                        selectedSliceIndex = params.selectedSliceIndex,
                        selectedScale = params.selectedScale.value,
                        textMeasurer = textMeasurer,
                    ),
            )
        }
        if (params.config.style == PieChartStyle.DONUT && params.centerContent != null) {
            Box(
                modifier = Modifier.fillMaxSize(params.config.donutHoleRatio),
                contentAlignment = Alignment.Center,
            ) {
                params.centerContent()
            }
        } else if (params.config.style == PieChartStyle.DONUT && params.config.shouldShowCenterText) {
            Text(
                text = params.total.toInt().toString(),
                style = params.config.centerTextStyle,
            )
        }
    }
}

/**
 * Draws all pie/donut slices with proper styling and animations
 */
private fun DrawScope.drawPieSlices(params: PieSliceDrawParams) {
    var currentAngle = params.config.startAngleDegrees

    params.dataList.fastForEachIndexed { index, slice ->
        val sweepAngle = slice.calculateSweepAngle(params.total) * params.animationProgress
        val percentage = slice.calculatePercentage(params.total)
        val sliceColor = params.colors.getOrElse(index) { params.colors.first() }

        if (sweepAngle > 0) {
            val isSelected = index == params.selectedSliceIndex
            val scale =
                if (isSelected) {
                    params.selectedScale
                } else {
                    1f
                }
            val alpha =
                if (params.selectedSliceIndex != null && !isSelected) {
                    params.config.interactionConfig.unselectedSliceOpacity
                } else {
                    1f
                }
            val sliceCenter = params.center
            val offset =
                if (isSelected && params.config.interactionConfig.isEnabled) {
                    val angle = (currentAngle + sweepAngle / HALF_DIVIDER) * DEGREES_TO_RADIANS
                    Offset(
                        x = (cos(angle) * params.config.interactionConfig.selectedSlicePullOutDistance).toFloat(),
                        y = (sin(angle) * params.config.interactionConfig.selectedSlicePullOutDistance).toFloat(),
                    )
                } else {
                    Offset.Zero
                }

            val actualRadius = params.radius * scale
            val drawCenter = sliceCenter + offset
            when (params.config.style) {
                PieChartStyle.PIE -> {
                    drawArc(
                        brush = sliceColor.toSliceBrush(),
                        startAngle = currentAngle + params.config.sliceSpacingDegrees / HALF_DIVIDER,
                        sweepAngle = max(0f, sweepAngle - params.config.sliceSpacingDegrees),
                        useCenter = true,
                        topLeft =
                            Offset(
                                drawCenter.x - actualRadius,
                                drawCenter.y - actualRadius,
                            ),
                        size = Size(actualRadius * DOUBLE_MULTIPLIER, actualRadius * DOUBLE_MULTIPLIER),
                        alpha = alpha,
                    )
                }

                PieChartStyle.DONUT -> {
                    val strokeWidth = actualRadius * (1f - params.config.donutHoleRatio)
                    val arcRadius = actualRadius - strokeWidth / HALF_DIVIDER

                    drawArc(
                        brush = sliceColor.toSliceBrush(),
                        startAngle = currentAngle + params.config.sliceSpacingDegrees / HALF_DIVIDER,
                        sweepAngle = max(0f, sweepAngle - params.config.sliceSpacingDegrees),
                        useCenter = false,
                        topLeft =
                            Offset(
                                drawCenter.x - arcRadius,
                                drawCenter.y - arcRadius,
                            ),
                        size = Size(arcRadius * DOUBLE_MULTIPLIER, arcRadius * DOUBLE_MULTIPLIER),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                        alpha = alpha,
                    )
                }
            }
            if (params.config.labelConfig.shouldShowLabels &&
                percentage >= params.config.labelConfig.minimumPercentageToShowLabel &&
                params.animationProgress > LABEL_ANIMATION_THRESHOLD
            ) {
                drawSliceLabel(
                    slice = slice,
                    percentage = percentage,
                    center = drawCenter,
                    radius = actualRadius,
                    angle = currentAngle + sweepAngle / HALF_DIVIDER,
                    config = params.config,
                    textMeasurer = params.textMeasurer,
                )
            }

            currentAngle += sweepAngle
        }
    }
}

/**
 * Draws label text on a slice
 */
private fun DrawScope.drawSliceLabel(
    slice: PieData,
    percentage: Float,
    center: Offset,
    radius: Float,
    angle: Float,
    config: PieChartConfig,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    val labelText =
        buildString {
            if (config.labelConfig.shouldShowPercentage) {
                append("${(percentage * PERCENTAGE_PRECISION_MULTIPLIER).toInt() / PERCENTAGE_PRECISION_MULTIPLIER}%")
            }
            if (config.labelConfig.shouldShowValue) {
                if (isNotEmpty()) {
                    append("\n")
                }
                append(slice.value.toInt().toString())
            }
        }

    if (labelText.isEmpty()) {
        return
    }

    val textLayoutResult = textMeasurer.measure(labelText, config.labelConfig.labelTextStyle)
    val labelRadius =
        when (config.style) {
            PieChartStyle.PIE -> radius * PIE_LABEL_RADIUS_MULTIPLIER
            PieChartStyle.DONUT -> radius * (1f - config.donutHoleRatio / DONUT_LABEL_RADIUS_DIVISOR)
        }

    val angleRad = angle * DEGREES_TO_RADIANS
    val labelX = center.x + cos(angleRad).toFloat() * labelRadius - textLayoutResult.size.width / HALF_DIVIDER
    val labelY = center.y + sin(angleRad).toFloat() * labelRadius - textLayoutResult.size.height / HALF_DIVIDER

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(labelX, labelY),
    )
}

/**
 * Finds which slice was clicked based on touch coordinates
 */
private fun findClickedSlice(
    touchPosition: Offset,
    center: Offset,
    radius: Float,
    dataList: List<PieData>,
    total: Float,
    config: PieChartConfig,
): Int? {
    val dx = touchPosition.x - center.x
    val dy = touchPosition.y - center.y
    val distance = sqrt(dx * dx + dy * dy)
    val innerRadius =
        if (config.style == PieChartStyle.DONUT) {
            radius * config.donutHoleRatio
        } else {
            0f
        }

    if (distance !in innerRadius..radius) {
        return null
    }

    var touchAngle = (atan2(dy.toDouble(), dx.toDouble()) * RADIANS_TO_DEGREES).toFloat()
    if (touchAngle < 0) {
        touchAngle += FULL_CIRCLE_DEGREES
    }
    var normalizedAngle = touchAngle - config.startAngleDegrees
    if (normalizedAngle < 0) {
        normalizedAngle += FULL_CIRCLE_DEGREES
    }
    var currentAngle = 0f
    var result: Int? = null
    dataList.fastForEachIndexed { index, slice ->
        if (result == null) {
            val sweepAngle = slice.calculateSweepAngle(total)
            if (normalizedAngle >= currentAngle && normalizedAngle < currentAngle + sweepAngle) {
                result = index
            }
            currentAngle += sweepAngle
        }
    }

    return result
}

/**
 * Generates colors for slices based on ChartyColor configuration
 */
private fun generateSliceColors(
    dataList: List<PieData>,
    color: ChartyColor,
): List<ChartyColor> {
    val customColors = dataList.mapNotNull { it.color }
    if (customColors.size == dataList.size) {
        return customColors
    }
    return when (color) {
        is ChartyColor.Solid -> List(dataList.size) { color }
        is ChartyColor.Gradient ->
            List(dataList.size) { index -> ChartyColor.Solid(color.colors[index % color.colors.size]) }
    }
}

/** Resolves a [ChartyColor] to a [Brush] for filling a slice (solid or gradient). */
private fun ChartyColor.toSliceBrush(): Brush =
    when (this) {
        is ChartyColor.Solid -> SolidColor(color)
        is ChartyColor.Gradient -> Brush.linearGradient(colors)
    }
