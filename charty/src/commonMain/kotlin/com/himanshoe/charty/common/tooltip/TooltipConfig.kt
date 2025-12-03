package com.himanshoe.charty.common.tooltip

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DEFAULT_COLOR = Color(0xFF2D2D2D)

/**
 * Configuration for tooltip appearance and positioning
 *
 * @param shape The shape of the tooltip background
 * @param backgroundColor Background color of the tooltip
 * @param borderColor Color of the tooltip border (null for no border)
 * @param borderWidth Width of the tooltip border
 * @param textStyle Style for the tooltip text
 * @param padding Padding inside the tooltip
 * @param elevation Shadow elevation for the tooltip
 * @param offsetY Vertical offset from the bar top (positive = below bar, negative = above bar)
 * @param minDistanceFromEdge Minimum distance from chart edges before repositioning
 * @param showArrow Whether to show an arrow pointing to the bar
 * @param arrowSize Size of the arrow if shown
 */
data class TooltipConfig(
    val shape: Shape = RoundedCornerShape(8.dp),
    val backgroundColor: Color = DEFAULT_COLOR,
    val borderColor: Color? = null,
    val borderWidth: Dp = 1.dp,
    val textStyle: TextStyle = TextStyle(
        color = Color.White,
        fontSize = 14.sp,
    ),
    val padding: TooltipPadding = TooltipPadding(),
    val elevation: Dp = 4.dp,
    val offsetY: Dp = 8.dp,
    val minDistanceFromEdge: Dp = 16.dp,
    val showArrow: Boolean = true,
    val arrowSize: Dp = 8.dp,
)

/**
 * Padding configuration for tooltip content
 */
data class TooltipPadding(
    val horizontal: Dp = 12.dp,
    val vertical: Dp = 8.dp,
)

/**
 * Position where the tooltip should be displayed relative to the bar
 */
enum class TooltipPosition {
    /** Above the bar */
    ABOVE,

    /** Below the bar */
    BELOW,

    /** Automatically position based on available space */
    AUTO,
}

/**
 * Data class representing the content and position of a tooltip
 *
 * @param content The text content to display in the tooltip
 * @param x X coordinate of the bar center
 * @param y Y coordinate of the bar top
 * @param barWidth Width of the bar
 * @param position Preferred position of the tooltip
 */
data class TooltipState(
    val content: String,
    val x: Float,
    val y: Float,
    val barWidth: Float,
    val position: TooltipPosition = TooltipPosition.AUTO,
)

