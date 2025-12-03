package com.himanshoe.charty.common


/**
 * Specifies the orientation of a chart, determining the arrangement of its axes and data representation.
 * The orientation affects how components like bars, lines, and axes are displayed.
 */
enum class ChartOrientation {
    /**
     * Represents a vertical orientation for the chart.
     * In this layout:
     * - The X-axis is positioned horizontally at the bottom.
     * - The Y-axis is positioned vertically on the left.
     * - Data elements, such as bars, grow upwards from the X-axis.
     * - Values are typically plotted along the Y-axis, with corresponding labels on the X-axis.
     */
    VERTICAL,

    /**
     * Represents a horizontal orientation for the chart.
     * In this layout:
     * - The X-axis is positioned vertically on the left.
     * - The Y-axis is positioned horizontally at the bottom.
     * - Data elements, such as bars, grow rightward from the Y-axis.
     * - Values are typically plotted along the X-axis, with corresponding labels on the Y-axis.
     */
    HORIZONTAL,
}
