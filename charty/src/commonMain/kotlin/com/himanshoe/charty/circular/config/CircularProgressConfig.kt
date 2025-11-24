package com.himanshoe.charty.circular.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.config.Animation

private const val DEFAULT_START_ANGLE_DEGREES = -90f

/**
 * Direction for drawing the circular rings
 */
enum class RingDirection {
    /**
     * Clockwise direction (default, like Apple Activity Rings)
     */
    CLOCKWISE,

    /**
     * Counter-clockwise direction
     */
    COUNTER_CLOCKWISE,
}

/**
 * Comprehensive configuration for CircularProgressIndicator appearance and behavior
 *
 * @param gapBetweenRings Gap between concentric rings in pixels
 * @param startAngleDegrees Starting angle in degrees (-90° = top, 0° = right, 90° = bottom, 180° = left)
 * @param ringDirection Direction to draw rings (CLOCKWISE or COUNTER_CLOCKWISE)
 * @param strokeCap Style of line ends (Round, Butt, or Square)
 * @param animation Animation configuration for entry and transitions
 * @param enableShadows Whether to enable shadows for all rings (individual rings can still override)
 * @param centerHoleRatio Ratio of the center hole size (0.0 = no hole, 1.0 = all hole)
 * @param rotationEnabled Whether the entire ring system should rotate
 * @param rotationDurationMs Duration of one full rotation in milliseconds (only if rotationEnabled)
 * @param interactionEnabled Whether rings respond to clicks/taps
 *
 * Usage:
 * ```kotlin
 * // Default configuration (similar to Apple Activity Rings)
 * val defaultConfig = CircularProgressConfig()
 *
 * // Custom configuration with larger gaps and custom angles
 * val customConfig = CircularProgressConfig(
 *     gapBetweenRings = 16f,
 *     startAngleDegrees = -90f, // Start at top
 *     strokeCap = StrokeCap.Round,
 *     animation = Animation.Enabled(duration = 1500)
 * )
 *
 * // Configuration with shadows enabled
 * val shadowConfig = CircularProgressConfig(
 *     enableShadows = true,
 *     gapBetweenRings = 12f,
 *     animation = Animation.Slow
 * )
 * ```
 */
data class CircularProgressConfig(
    /**
     * Gap between concentric rings in pixels (default: 8f)
     */
    val gapBetweenRings: Float = 8f,
    /**
     * Starting angle in degrees (default: -90f for top position)
     * - -90° = top (12 o'clock position)
     * - 0° = right (3 o'clock position)
     * - 90° = bottom (6 o'clock position)
     * - 180° = left (9 o'clock position)
     */
    val startAngleDegrees: Float = DEFAULT_START_ANGLE_DEGREES,
    /**
     * Direction to draw the rings (default: CLOCKWISE)
     */
    val ringDirection: RingDirection = RingDirection.CLOCKWISE,
    /**
     * Style of the stroke cap (default: Round for smooth appearance)
     */
    val strokeCap: StrokeCap = StrokeCap.Round,
    /**
     * Animation configuration (default: enabled with 800ms duration)
     */
    val animation: Animation = Animation.Default,
    /**
     * Whether to enable shadows globally (default: false for better performance)
     * Individual rings can still specify their own shadow properties
     */
    val enableShadows: Boolean = false,
    /**
     * Ratio of the center hole size to the overall size (0.0 to 0.5)
     * 0.0 = no center hole (rings fill inward completely)
     * 0.2 = small center hole
     * Higher values leave more space in the center
     */
    val centerHoleRatio: Float = 0.0f,
    /**
     * Whether the entire ring system should rotate continuously
     */
    val rotationEnabled: Boolean = false,
    /**
     * Duration of one full rotation in milliseconds (only used if rotationEnabled = true)
     */
    val rotationDurationMs: Int = 3000,
    /**
     * Whether rings respond to click/tap interactions
     */
    val interactionEnabled: Boolean = true,
    /**
     * Whether to show progress text in the center
     */
    val showCenterText: Boolean = false,
    /**
     * Padding around the entire circular progress indicator (to accommodate shadows)
     */
    val paddingDp: Float = 16f,
    /**
     * TextStyle for center text - allows full customization of text appearance
     */
    val centerTextStyle: TextStyle =
        TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        ),
) {
    init {
        require(gapBetweenRings >= 0f) {
            "gapBetweenRings must be non-negative, got: $gapBetweenRings"
        }
        require(centerHoleRatio in 0f..0.5f) {
            "centerHoleRatio must be between 0.0 and 0.5, got: $centerHoleRatio"
        }
        require(rotationDurationMs > 0) {
            "rotationDurationMs must be positive, got: $rotationDurationMs"
        }
        require(paddingDp >= 0f) {
            "paddingDp must be non-negative, got: $paddingDp"
        }
    }
}
