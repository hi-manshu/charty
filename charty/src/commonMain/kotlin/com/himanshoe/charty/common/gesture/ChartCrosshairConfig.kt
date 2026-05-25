package com.himanshoe.charty.common.gesture

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.tooltip.TooltipConfig

/**
 * Configuration for the draggable crosshair overlay.
 *
 * When enabled on a line or point chart, the crosshair tracks the user's finger, snapping
 * to the nearest data point and showing a value label. Enabling the crosshair replaces
 * the standard tap-to-tooltip interaction.
 *
 * @property verticalLineColor Color of the vertical crosshair line. Accepts [ChartyColor.Solid]
 *   or [ChartyColor.Gradient]; a gradient is applied top-to-bottom along the line.
 * @property horizontalLineColor Color of the horizontal crosshair line. Accepts [ChartyColor.Solid]
 *   or [ChartyColor.Gradient]; a gradient is applied left-to-right along the line.
 * @property lineWidth Stroke width for both crosshair lines in pixels.
 * @property showHorizontalLine Whether to draw the horizontal crosshair line.
 * @property dotRadius Radius of the highlight circle drawn at the snapped data point.
 * @property showLabel Whether to display the value label above the crosshair intersection.
 * @property tooltipConfig Appearance configuration for the value label bubble.
 * @property dismissOnRelease Whether to hide the crosshair when the finger lifts. Set to
 *   `false` to keep it pinned after release, useful for inspecting a specific value.
 */
data class ChartCrosshairConfig(
    val verticalLineColor: ChartyColor = ChartyColor.Solid(Color.Black.copy(alpha = 0.4f)),
    val horizontalLineColor: ChartyColor = ChartyColor.Solid(Color.Black.copy(alpha = 0.15f)),
    val lineWidth: Float = 1.5f,
    val showHorizontalLine: Boolean = true,
    val dotRadius: Float = 8f,
    val showLabel: Boolean = true,
    val tooltipConfig: TooltipConfig = TooltipConfig(showArrow = false),
    val dismissOnRelease: Boolean = true,
)
