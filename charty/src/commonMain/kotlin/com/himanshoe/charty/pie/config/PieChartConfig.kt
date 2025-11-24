package com.himanshoe.charty.pie.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.config.Animation

private const val DEFAULT_CENTER_TEXT_SIZE_SP = 16f
private const val MAX_DONUT_HOLE_RATIO = 0.9f
private const val MAX_SLICE_SPACING_DEGREES = 10f

/**
 * Style for pie/donut chart visualization
 */
enum class PieChartStyle {
    /**
     * Traditional pie chart - full circle with no center hole
     */
    PIE,

    /**
     * Donut chart - circular chart with a center hole
     */
    DONUT
}

/**
 * Configuration for label display on slices
 */
data class LabelConfig(
    /**
     * Whether to display labels on slices
     */
    val shouldShowLabels: Boolean = true,

    /**
     * Whether to display percentage values on labels
     */
    val shouldShowPercentage: Boolean = true,

    /**
     * Whether to display actual numeric values on labels
     */
    val shouldShowValue: Boolean = false,

    /**
     * Minimum percentage threshold to display a label (avoids clutter on tiny slices)
     * For example, 3f means only show labels for slices >= 3% of total
     */
    val minimumPercentageToShowLabel: Float = 3f,

    /**
     * Text size for labels in SP (Scalable Pixels) - deprecated, use labelTextStyle instead
     */
    @Deprecated("Use labelTextStyle instead for more control")
    val labelTextSize: Float = 12f,

    /**
     * Whether to show labels outside the chart with connecting lines (future feature)
     */
    val shouldShowLabelsOutside: Boolean = false,

    /**
     * TextStyle for labels on slices - allows full customization of text appearance
     */
    val labelTextStyle: TextStyle = TextStyle(
        fontSize = 12.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )
) {
    init {
        require(minimumPercentageToShowLabel in 0f..100f) {
            "minimumPercentageToShowLabel must be between 0 and 100, got: $minimumPercentageToShowLabel"
        }
        @Suppress("DEPRECATION")
        require(labelTextSize > 0f) {
            "labelTextSize must be positive, got: $labelTextSize"
        }
    }
}

/**
 * Configuration for slice interaction effects
 */
data class InteractionConfig(
    /**
     * Whether slices are clickable
     */
    val isEnabled: Boolean = true,

    /**
     * Scale multiplier when a slice is selected (1.0 = no scaling, 1.1 = 10% larger)
     */
    val selectedScaleMultiplier: Float = 1.1f,

    /**
     * Distance in pixels to pull out a selected slice from center
     */
    val selectedSlicePullOutDistance: Float = 8f,

    /**
     * Duration of selection animation in milliseconds
     */
    val selectionAnimationDurationMs: Int = 200,

    /**
     * Whether to enable hover effects (useful for desktop/web platforms)
     */
    val enableHoverEffect: Boolean = true,

    /**
     * Opacity for non-selected slices when one is selected (0.0 = transparent, 1.0 = opaque)
     */
    val unselectedSliceOpacity: Float = 0.6f
) {
    init {
        require(selectedScaleMultiplier >= 1f) {
            "selectedScaleMultiplier must be >= 1.0, got: $selectedScaleMultiplier"
        }
        require(selectedSlicePullOutDistance >= 0f) {
            "selectedSlicePullOutDistance must be non-negative, got: $selectedSlicePullOutDistance"
        }
        require(selectionAnimationDurationMs > 0) {
            "selectionAnimationDurationMs must be positive, got: $selectionAnimationDurationMs"
        }
        require(unselectedSliceOpacity in 0f..1f) {
            "unselectedSliceOpacity must be between 0 and 1, got: $unselectedSliceOpacity"
        }
    }
}

/**
 * Comprehensive configuration for Pie/Donut Chart appearance and behavior
 *
 * @param style Chart visual style - PIE (full circle) or DONUT (with center hole)
 * @param donutHoleRatio Ratio of center hole size to chart radius (0.0 - 0.9). Only applies to DONUT style
 * @param startAngleDegrees Starting angle in degrees (0° = right, -90° = top, 180° = left, 90° = bottom)
 * @param labelConfig Configuration for slice labels (text displayed on slices)
 * @param interactionConfig Configuration for click/hover/selection interactions
 * @param animation Animation configuration for entry and transitions
 * @param sliceSpacingDegrees Gap between slices in degrees (0 for no gap, typical: 2-5)
 * @param shouldShowCenterText Whether to show numeric text in the center of donut charts
 * @param centerTextStyle TextStyle for center text - allows full customization of text appearance
 *
 * Usage:
 * ```kotlin
 * // Simple pie chart with defaults
 * val pieConfig = PieChartConfig()
 *
 * // Donut chart with customization
 * val donutConfig = PieChartConfig(
 *     style = PieChartStyle.DONUT,
 *     donutHoleRatio = 0.6f,
 *     labelConfig = LabelConfig(shouldShowLabels = false)
 * )
 *
 * // Interactive pie with animations
 * val interactiveConfig = PieChartConfig(
 *     animation = Animation.Enabled(duration = 1000),
 *     interactionConfig = InteractionConfig(
 *         selectedScaleMultiplier = 1.15f,
 *         selectedSlicePullOutDistance = 12f
 *     ),
 *     sliceSpacingDegrees = 3f
 * )
 * ```
 */
data class PieChartConfig(
    val style: PieChartStyle = PieChartStyle.PIE,
    val donutHoleRatio: Float = 0.5f,
    val startAngleDegrees: Float = -90f,
    val labelConfig: LabelConfig = LabelConfig(),
    val interactionConfig: InteractionConfig = InteractionConfig(),
    val animation: Animation = Animation.Default,
    val sliceSpacingDegrees: Float = 0f,
    val shouldShowCenterText: Boolean = false,
    val centerTextStyle: TextStyle = TextStyle(
        fontSize = DEFAULT_CENTER_TEXT_SIZE_SP.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
) {
    init {
        require(donutHoleRatio in 0f..MAX_DONUT_HOLE_RATIO) {
            "donutHoleRatio must be between 0 and $MAX_DONUT_HOLE_RATIO, got: $donutHoleRatio"
        }
        require(sliceSpacingDegrees in 0f..MAX_SLICE_SPACING_DEGREES) {
            "sliceSpacingDegrees must be between 0 and $MAX_SLICE_SPACING_DEGREES degrees, got: $sliceSpacingDegrees"
        }
    }
}
