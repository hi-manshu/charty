package com.himanshoe.charty.pie.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ReferenceLineConfig

private const val DEFAULT_CENTER_TEXT_SIZE_SP = 16f
private const val MAX_DONUT_HOLE_RATIO = 0.9f
private const val MAX_SLICE_SPACING_DEGREES = 10f
private const val DEFAULT_LABEL_SIZE_SP = 12f
private const val MIN_PERCENTAGE_THRESHOLD = 3f
private const val DEFAULT_SELECTED_SCALE = 1.1f
private const val DEFAULT_PULL_OUT_DISTANCE = 8f
private const val DEFAULT_ANIMATION_DURATION_MS = 200
private const val DEFAULT_UNSELECTED_OPACITY = 0.6f
private const val MIN_SCALE_MULTIPLIER = 1f
private const val MIN_PERCENTAGE = 0f
private const val MAX_PERCENTAGE = 100f
private const val MIN_OPACITY = 0f
private const val MAX_OPACITY = 1f
private const val DEFAULT_DONUT_HOLE_RATIO = 0.5f
private const val DEFAULT_START_ANGLE_DEGREES = -90f
private const val DEFAULT_SLICE_SPACING_DEGREES = 0f

/**
 * Style for pie/donut chart visualization.
 *
 * Defines the visual appearance of the chart as either a traditional
 * pie chart with a full circle or a donut chart with a center hole.
 */
enum class PieChartStyle {
    /** Traditional pie chart with full circle and no center hole */
    PIE,

    /** Donut chart with a center hole defined by donutHoleRatio */
    DONUT,
}

/**
 * Configuration for label display on pie chart slices.
 *
 * Controls how labels are shown on chart slices including visibility,
 * content format, and text styling.
 *
 * @property shouldShowLabels Whether to display labels on slices
 * @property shouldShowPercentage Whether to display percentage values
 * @property shouldShowValue Whether to display actual numeric values
 * @property minimumPercentageToShowLabel Minimum percentage threshold to display a label
 * @property shouldShowLabelsOutside Whether to show labels outside the chart
 * @property labelTextStyle TextStyle for customizing label appearance
 */
data class LabelConfig(
    val shouldShowLabels: Boolean = true,
    val shouldShowPercentage: Boolean = true,
    val shouldShowValue: Boolean = false,
    val minimumPercentageToShowLabel: Float = MIN_PERCENTAGE_THRESHOLD,
    val shouldShowLabelsOutside: Boolean = false,
    val labelTextStyle: TextStyle = TextStyle(
        fontSize = DEFAULT_LABEL_SIZE_SP.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold,
    ),
) {
    init {
        require(minimumPercentageToShowLabel in MIN_PERCENTAGE..MAX_PERCENTAGE) {
            "minimumPercentageToShowLabel must be between $MIN_PERCENTAGE and $MAX_PERCENTAGE"
        }
    }
}

/**
 * Configuration for slice interaction effects and animations.
 *
 * Controls how slices respond to user interactions including clicks,
 * hover effects, and selection animations.
 *
 * @property isEnabled Whether slices are clickable and interactive
 * @property selectedScaleMultiplier Scale multiplier applied when a slice is selected
 * @property selectedSlicePullOutDistance Distance in pixels to pull out selected slice from center
 * @property selectionAnimationDurationMs Duration of selection animation in milliseconds
 * @property enableHoverEffect Whether to enable hover effects (useful for desktop/web)
 * @property unselectedSliceOpacity Opacity for non-selected slices when one is selected
 */
data class InteractionConfig(
    val isEnabled: Boolean = true,
    val selectedScaleMultiplier: Float = DEFAULT_SELECTED_SCALE,
    val selectedSlicePullOutDistance: Float = DEFAULT_PULL_OUT_DISTANCE,
    val selectionAnimationDurationMs: Int = DEFAULT_ANIMATION_DURATION_MS,
    val enableHoverEffect: Boolean = true,
    val unselectedSliceOpacity: Float = DEFAULT_UNSELECTED_OPACITY,
) {
    init {
        require(selectedScaleMultiplier >= MIN_SCALE_MULTIPLIER) {
            "selectedScaleMultiplier must be >= $MIN_SCALE_MULTIPLIER"
        }
        require(selectedSlicePullOutDistance >= MIN_PERCENTAGE) {
            "selectedSlicePullOutDistance must be non-negative"
        }
        require(selectionAnimationDurationMs > 0) {
            "selectionAnimationDurationMs must be positive"
        }
        require(unselectedSliceOpacity in MIN_OPACITY..MAX_OPACITY) {
            "unselectedSliceOpacity must be between $MIN_OPACITY and $MAX_OPACITY"
        }
    }
}

/**
 * Comprehensive configuration for Pie/Donut Chart appearance and behavior.
 *
 * @property style Chart visual style (PIE or DONUT)
 * @property donutHoleRatio Ratio of center hole size to chart radius
 * @property startAngleDegrees Starting angle in degrees
 * @property labelConfig Configuration for slice labels
 * @property interactionConfig Configuration for interactions
 * @property animation Animation configuration
 * @property sliceSpacingDegrees Gap between slices in degrees
 * @property shouldShowCenterText Whether to show text in center
 * @property centerTextStyle TextStyle for center text
 * @property referenceLine Optional reference line configuration
 */
data class PieChartConfig(
    val style: PieChartStyle = PieChartStyle.PIE,
    val donutHoleRatio: Float = DEFAULT_DONUT_HOLE_RATIO,
    val startAngleDegrees: Float = DEFAULT_START_ANGLE_DEGREES,
    val labelConfig: LabelConfig = LabelConfig(),
    val interactionConfig: InteractionConfig = InteractionConfig(),
    val animation: Animation = Animation.Default,
    val sliceSpacingDegrees: Float = DEFAULT_SLICE_SPACING_DEGREES,
    val shouldShowCenterText: Boolean = false,
    val centerTextStyle: TextStyle = TextStyle(
        fontSize = DEFAULT_CENTER_TEXT_SIZE_SP.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
    ),
    val referenceLine: ReferenceLineConfig? = null,
) {
    init {
        require(donutHoleRatio in MIN_PERCENTAGE..MAX_DONUT_HOLE_RATIO) {
            "donutHoleRatio must be between $MIN_PERCENTAGE and $MAX_DONUT_HOLE_RATIO"
        }
        require(sliceSpacingDegrees in MIN_PERCENTAGE..MAX_SLICE_SPACING_DEGREES) {
            "sliceSpacingDegrees must be between $MIN_PERCENTAGE and $MAX_SLICE_SPACING_DEGREES"
        }
    }
}
