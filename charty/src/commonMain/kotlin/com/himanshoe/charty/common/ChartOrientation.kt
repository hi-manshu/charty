package com.himanshoe.charty.common


/**
 * Orientation of the chart - determines how axes and data are arranged
 */
enum class ChartOrientation {
    /**
     * Vertical orientation - X-axis horizontal at bottom, Y-axis vertical on left
     * Bars grow upward, values on Y-axis, labels on X-axis
     */
    VERTICAL,

    /**
     * Horizontal orientation - X-axis vertical on left, Y-axis horizontal at bottom
     * Bars grow rightward, values on X-axis, labels on Y-axis
     */
    HORIZONTAL,
}
