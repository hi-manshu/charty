package com.himanshoe.charty.common.axis

/**
 * A sealed class that defines the rotation angle for axis labels.
 *
 * This class provides a type-safe way to specify common rotation angles for labels,
 * avoiding the use of raw float values and improving code readability.
 *
 * @property degrees The rotation angle in degrees.
 */
sealed class LabelRotation(val degrees: Float) {

    /**
     * Represents no rotation, where labels are displayed horizontally.
     */
    data object Straight : LabelRotation(ANGLE_STRAIGHT)

    /**
     * Represents a 45-degree clockwise rotation.
     */
    data object Angle45 : LabelRotation(ANGLE_45_DEGREES)

    /**
     * Represents a 45-degree counter-clockwise rotation.
     */
    data object Angle45Negative : LabelRotation(-ANGLE_45_DEGREES)

    /**
     * Represents a 90-degree clockwise rotation, making the labels vertical.
     */
    data object Vertical : LabelRotation(ANGLE_90_DEGREES)

    /**
     * Represents a 90-degree counter-clockwise rotation, making the labels vertical.
     */
    data object VerticalNegative : LabelRotation(-ANGLE_90_DEGREES)

    /**
     * Represents a custom rotation angle.
     *
     * @param angle The rotation angle in degrees.
     */
    data class Custom(val angle: Float) : LabelRotation(angle)

    /**
     * A companion object that holds constants for common rotation angles.
     */
    companion object {
        private const val ANGLE_STRAIGHT = 0F
        private const val ANGLE_45_DEGREES = 45F
        private const val ANGLE_90_DEGREES = 90F
    }
}

