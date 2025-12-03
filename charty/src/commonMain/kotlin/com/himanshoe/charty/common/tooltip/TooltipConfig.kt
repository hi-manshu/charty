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
 * A data class that holds the configuration for the appearance and positioning of a tooltip.
 *
 * @property shape The shape of the tooltip's background.
 * @property backgroundColor The background color of the tooltip.
 * @property borderColor The color of the tooltip's border. If `null`, no border is drawn.
 * @property borderWidth The width of the tooltip's border.
 * @property textStyle The [TextStyle] for the tooltip's text content.
 * @property padding The padding inside the tooltip, defined by [TooltipPadding].
 * @property elevation The shadow elevation of the tooltip.
 * @property offsetY The vertical offset from the top of the bar (positive values move it down, negative values move it up).
 * @property minDistanceFromEdge The minimum distance the tooltip should maintain from the edges of the chart before being repositioned.
 * @property showArrow Determines whether to show an arrow pointing to the bar.
 * @property arrowSize The size of the arrow, if shown.
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
 * A data class that defines the padding for a tooltip's content.
 *
 * @property horizontal The horizontal padding.
 * @property vertical The vertical padding.
 */
data class TooltipPadding(
    val horizontal: Dp = 12.dp,
    val vertical: Dp = 8.dp,
)

/**
 * An enum that specifies the desired position of a tooltip relative to its associated bar or point.
 */
enum class TooltipPosition {
    /** The tooltip should be displayed above the bar/point. */
    ABOVE,

    /** The tooltip should be displayed below the bar/point. */
    BELOW,

    /** The tooltip's position should be automatically determined based on the available space. */
    AUTO,
}

/**
 * A data class that represents the state of a tooltip, including its content and position.
 *
 * @property content The text content to be displayed in the tooltip.
 * @property x The x-coordinate of the bar's center.
 * @property y The y-coordinate of the bar's top.
 * @property barWidth The width of the bar.
 * @property position The preferred position of the tooltip, defined by [TooltipPosition].
 */
data class TooltipState(
    val content: String,
    val x: Float,
    val y: Float,
    val barWidth: Float,
    val position: TooltipPosition = TooltipPosition.AUTO,
)

