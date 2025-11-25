@file:Suppress("MagicNumber")

package com.himanshoe.charty.common.axis

/**
 * Defines the rotation angle for axis labels.
 * Provides type-safe options for label rotation instead of raw float values.
 */
sealed class LabelRotation(val degrees: Float) {

    /**
     * No rotation - labels are displayed horizontally
     */
    data object Straight : LabelRotation(0F)

    /**
     * 45-degree rotation
     */
    data object Angle45 : LabelRotation(45F)

    /**
     * -45-degree rotation (counter-clockwise)
     */
    data object Angle45Negative : LabelRotation(-45F)

    /**
     * 90-degree rotation (vertical)
     */
    data object Vertical : LabelRotation(90F)

    /**
     * -90-degree rotation (vertical, counter-clockwise)
     */
    data object VerticalNegative : LabelRotation(-90F)

    /**
     * Custom rotation angle
     *
     * @param angle The rotation angle in degrees
     */
    data class Custom(val angle: Float) : LabelRotation(angle)
}

