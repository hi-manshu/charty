package com.himanshoe.charty.pie

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.accessibility.generatePieChartDescription
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.pie.config.PieChartConfig
import com.himanshoe.charty.pie.config.PieChartStyle
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty.pie.internal.PieChartContent
import com.himanshoe.charty.pie.internal.PieChartContentParams
import com.himanshoe.charty.pie.internal.generateSliceColors
import com.himanshoe.charty.pie.internal.onSliceClickLambda

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
    val dataList by remember(data) { derivedStateOf { data() } }
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
