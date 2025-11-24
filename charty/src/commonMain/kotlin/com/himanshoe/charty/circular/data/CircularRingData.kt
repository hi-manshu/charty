package com.himanshoe.charty.circular.data

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor

/**
 * Data class representing a single ring in the CircularProgressIndicator
 *
 * Similar to Apple Activity Rings, where each ring represents a different metric
 * with its own progress, colors, and visual properties.
 *
 * @param label The label/name for this ring (e.g., "Move", "Exercise", "Stand")
 * @param progress Current progress value (0f to maxValue)
 * @param maxValue Maximum value for this ring (default 100f for percentage)
 * @param color Primary color for the filled portion of the ring (ChartyColor.Solid or Gradient)
 * @param backgroundColor Background color for the unfilled portion of the ring
 * @param shadowColor Optional shadow color for the ring (null for no shadow)
 * @param shadowRadius Shadow blur radius in pixels (0f for no shadow)
 * @param metadata Optional metadata for custom handling
 *
 * Note: Ring stroke width is automatically calculated based on available space and number of rings
 *
 * Usage:
 * ```kotlin
 * val moveRing = CircularRingData(
 *     label = "Move",
 *     progress = 450f,
 *     maxValue = 600f,
 *     color = ChartyColor.Solid(Color(0xFFFF3B58)),
 *     backgroundColor = ChartyColor.Solid(Color(0x33FF3B58)),
 *     shadowColor = Color(0xFFFF3B58),
 *     shadowRadius = 8f
 * )
 * ```
 */
data class CircularRingData(
    val label: String,
    val progress: Float,
    val maxValue: Float = 100f,
    val color: ChartyColor,
    val backgroundColor: ChartyColor? = null,
    val shadowColor: Color? = null,
    val shadowRadius: Float = 0f,
    val metadata: Map<String, Any>? = null
) {
    init {
        require(progress >= 0f) { "Progress must be non-negative, got: $progress" }
        require(maxValue > 0f) { "Max value must be positive, got: $maxValue" }
        require(label.isNotBlank()) { "Ring label cannot be blank" }
        require(shadowRadius >= 0f) { "Shadow radius must be non-negative, got: $shadowRadius" }
    }

    /**
     * Get the primary color for the ring (first color from ChartyColor)
     */
    fun getPrimaryColor(): Color {
        return color.value.first()
    }

    /**
     * Get the background color for the ring, with automatic fallback
     */
    fun getBackgroundColor(): Color {
        return backgroundColor?.value?.first() ?: getPrimaryColor().copy(alpha = 0.2f)
    }

    /**
     * Calculates the percentage progress (0f to 100f)
     */
    fun calculatePercentage(): Float {
        return ((progress / maxValue) * 100f).coerceIn(0f, 100f)
    }

    /**
     * Calculates the sweep angle in degrees for drawing (0f to 360f)
     */
    fun calculateSweepAngle(): Float {
        return ((progress / maxValue) * 360f).coerceIn(0f, 360f)
    }

    /**
     * Returns true if the ring is complete (progress >= maxValue)
     */
    fun isComplete(): Boolean {
        return progress >= maxValue
    }

    /**
     * Creates a copy with clamped progress (useful for animations)
     */
    fun withClampedProgress(newProgress: Float): CircularRingData {
        return copy(progress = newProgress.coerceIn(0f, maxValue))
    }
}