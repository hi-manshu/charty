package com.himanshoe.charty.common.axis

/**
 * Defines the rotation angle for axis labels.
 * Provides type-safe options for label rotation instead of raw float values.
 */
sealed class LabelRotation(val degrees: Float) {

    /**
     * No rotation - labels are displayed horizontally
     */
    data object Straight : LabelRotation(ANGLE_STRAIGHT)

    /**
     * 45-degree rotation
     */
    data object Angle45 : LabelRotation(ANGLE_45_DEGREES)

    /**
     * -45-degree rotation (counter-clockwise)
     */
    data object Angle45Negative : LabelRotation(-ANGLE_45_DEGREES)

    /**
     * 90-degree rotation (vertical)
     */
    data object Vertical : LabelRotation(ANGLE_90_DEGREES)

    /**
     * -90-degree rotation (vertical, counter-clockwise)
     */
    data object VerticalNegative : LabelRotation(-ANGLE_90_DEGREES)

    /**
     * Custom rotation angle
     *
     * @param angle The rotation angle in degrees
     */
    data class Custom(val angle: Float) : LabelRotation(angle)

    /**
     * Constants for common rotation angles
     */
    companion object {
        private const val ANGLE_STRAIGHT = 0F
        private const val ANGLE_45_DEGREES = 45F
        private const val ANGLE_90_DEGREES = 90F
    }
}

