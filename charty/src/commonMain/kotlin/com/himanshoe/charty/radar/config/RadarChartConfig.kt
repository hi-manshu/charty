package com.himanshoe.charty.radar.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation

private const val DEFAULT_GRID_LINE_WIDTH = 1f
private const val DEFAULT_AXIS_LINE_WIDTH = 1f
private const val DEFAULT_DATA_LINE_WIDTH = 2f
private const val DEFAULT_POINT_RADIUS = 4f
private const val DEFAULT_LABEL_DISTANCE_MULTIPLIER = 1.15f
private const val DEFAULT_LABEL_TEXT_SIZE_SP = 12f
private const val DEFAULT_VALUE_TEXT_SIZE_SP = 10f
private const val DEFAULT_CENTER_ICON_SIZE = 40f
private const val DEFAULT_PADDING_FRACTION = 0.15f
private const val DEFAULT_START_ANGLE = -90f
private const val GRID_LINE_ALPHA_DEFAULT = 0.5f
private const val DEFAULT_GRID_LINE_COLOR = 0xFFBDBDBD
private const val DEFAULT_AXIS_LINE_COLOR = 0xFF9E9E9E

/**
 * Style for radar chart grid
 */
enum class RadarGridStyle {
    /** Circular/web grid lines */
    CIRCULAR,

    /** Polygonal grid lines matching the number of axes */
    POLYGON,
}

/**
 * Configuration for radar chart labels
 *
 * @param showLabels Whether to show axis labels
 * @param showValues Whether to show values on data points
 * @param labelDistanceMultiplier Distance multiplier for label positioning (1.0 = at edge, >1.0 = outside)
 * @param labelTextStyle TextStyle for axis labels - allows full customization of text appearance
 * @param valueTextStyle TextStyle for value labels - allows full customization of text appearance
 */
data class RadarLabelConfig(
    val showLabels: Boolean = false,
    val showValues: Boolean = false,
    val labelDistanceMultiplier: Float = DEFAULT_LABEL_DISTANCE_MULTIPLIER,
    val labelTextStyle: TextStyle = TextStyle(color = Color.Black, fontSize = DEFAULT_LABEL_TEXT_SIZE_SP.sp),
    val valueTextStyle: TextStyle = TextStyle(color = Color.Black, fontSize = DEFAULT_VALUE_TEXT_SIZE_SP.sp),
) {
    init {
        require(labelDistanceMultiplier > 0f) { "Label distance multiplier must be positive" }
    }
}

/**
 * Configuration for radar chart grid appearance
 *
 * @param gridStyle Style of the grid (CIRCULAR or POLYGON)
 * @param numberOfGridLevels Number of concentric grid levels (excluding center)
 * @param showGridLines Whether to show grid lines
 * @param showAxisLines Whether to show axis lines from center to edges
 * @param gridLineWidth Width of grid lines
 * @param axisLineWidth Width of axis lines
 * @param gridLineColor Color of grid lines
 * @param axisLineColor Color of axis lines
 * @param gridLineAlpha Alpha transparency for grid lines
 */
data class RadarGridConfig(
    val gridStyle: RadarGridStyle = RadarGridStyle.POLYGON,
    val numberOfGridLevels: Int = 5,
    val showGridLines: Boolean = true,
    val showAxisLines: Boolean = true,
    val gridLineWidth: Float = DEFAULT_GRID_LINE_WIDTH,
    val axisLineWidth: Float = DEFAULT_AXIS_LINE_WIDTH,
    val gridLineColor: ChartyColor = ChartyColor.Solid(Color(DEFAULT_GRID_LINE_COLOR).copy(alpha = GRID_LINE_ALPHA_DEFAULT)),
    val axisLineColor: ChartyColor = ChartyColor.Solid(Color(DEFAULT_AXIS_LINE_COLOR).copy(alpha = 0.6f)),
    val gridLineAlpha: Float = GRID_LINE_ALPHA_DEFAULT,
) {
    init {
        require(numberOfGridLevels > 0) { "Number of grid levels must be positive" }
        require(gridLineWidth > 0f) { "Grid line width must be positive" }
        require(axisLineWidth > 0f) { "Axis line width must be positive" }
        require(gridLineAlpha in 0f..1f) { "Grid line alpha must be between 0 and 1" }
    }
}

/**
 * Configuration for radar chart center content
 *
 * @param showCenterIcon Whether to show an icon/content in the center
 * @param centerIconSize Size of the center icon/content
 * @param centerBackgroundColor Background color for center area
 * @param centerBackgroundRadius Radius of center background circle
 */
data class RadarCenterConfig(
    val showCenterIcon: Boolean = false,
    val centerIconSize: Float = DEFAULT_CENTER_ICON_SIZE,
    val centerBackgroundColor: Color = Color.Transparent,
    val centerBackgroundRadius: Float = 0f,
) {
    init {
        require(centerIconSize > 0f) { "Center icon size must be positive" }
        require(centerBackgroundRadius >= 0f) { "Center background radius must be non-negative" }
    }
}

/**
 * Comprehensive configuration for Radar Chart appearance and behavior
 *
 * @param dataLineWidth Width of the data polygon lines
 * @param showDataPoints Whether to show points at each data vertex
 * @param dataPointRadius Radius of data points
 * @param strokeCap Style of line ends
 * @param strokeJoin Style of line joins
 * @param startAngleDegrees Starting angle in degrees (0° = right, -90° = top)
 * @param labelConfig Configuration for labels
 * @param gridConfig Configuration for grid
 * @param centerConfig Configuration for center content
 * @param animation Animation configuration
 * @param scaleToFit Whether to scale the chart to fit available space
 * @param paddingFraction Padding around the chart as fraction of size
 */
data class RadarChartConfig(
    val dataLineWidth: Float = DEFAULT_DATA_LINE_WIDTH,
    val showDataPoints: Boolean = true,
    val dataPointRadius: Float = DEFAULT_POINT_RADIUS,
    val strokeCap: StrokeCap = StrokeCap.Round,
    val strokeJoin: StrokeJoin = StrokeJoin.Round,
    val startAngleDegrees: Float = DEFAULT_START_ANGLE,
    val labelConfig: RadarLabelConfig = RadarLabelConfig(),
    val gridConfig: RadarGridConfig = RadarGridConfig(),
    val centerConfig: RadarCenterConfig = RadarCenterConfig(),
    val animation: Animation = Animation.Default,
    val scaleToFit: Boolean = true,
    val paddingFraction: Float = DEFAULT_PADDING_FRACTION,
) {
    init {
        require(dataLineWidth > 0f) { "Data line width must be positive" }
        require(dataPointRadius >= 0f) { "Data point radius must be non-negative" }
        require(paddingFraction in 0f..0.5f) { "Padding fraction must be between 0 and 0.5" }
    }
}
