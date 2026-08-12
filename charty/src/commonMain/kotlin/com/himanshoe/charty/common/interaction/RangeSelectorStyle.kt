package com.himanshoe.charty.common.interaction

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor

private val DEFAULT_SELECTED_COLOR = Color(0xFF2D2D2D)
private val DEFAULT_UNSELECTED_COLOR = Color(0x142D2D2D)
private val DEFAULT_UNSELECTED_TEXT_COLOR = Color(0xFF5F6368)
private val DEFAULT_TEXT_STYLE = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)

/**
 * Styling for [ChartRangeSelector]'s pill buttons.
 *
 * Backgrounds accept any [ChartyColor]: a [ChartyColor.Gradient] fills the pill with a horizontal
 * gradient. Text colors are also [ChartyColor]; a gradient text color falls back to its first
 * color, since a pill label is too small for a legible gradient.
 *
 * @property selectedColor Background of the selected pill.
 * @property unselectedColor Background of every unselected pill.
 * @property selectedTextColor Label color of the selected pill; gradients use their first color.
 * @property unselectedTextColor Label color of unselected pills; gradients use their first color.
 * @property cornerRadius Corner radius of each pill's rounded-rectangle background; must be non-negative.
 * @property textStyle Base text style of the pill labels; its color is overridden per pill by
 *   [selectedTextColor] or [unselectedTextColor].
 * @property horizontalPadding Padding between a pill's label and its left/right edges; must be non-negative.
 * @property verticalPadding Padding between a pill's label and its top/bottom edges; must be non-negative.
 * @property itemSpacing Gap between neighbouring pills; must be non-negative.
 */
@Stable
data class RangeSelectorStyle(
    val selectedColor: ChartyColor = ChartyColor.Solid(color = DEFAULT_SELECTED_COLOR),
    val unselectedColor: ChartyColor = ChartyColor.Solid(color = DEFAULT_UNSELECTED_COLOR),
    val selectedTextColor: ChartyColor = ChartyColor.Solid(color = Color.White),
    val unselectedTextColor: ChartyColor = ChartyColor.Solid(color = DEFAULT_UNSELECTED_TEXT_COLOR),
    val cornerRadius: Dp = 16.dp,
    val textStyle: TextStyle = DEFAULT_TEXT_STYLE,
    val horizontalPadding: Dp = 12.dp,
    val verticalPadding: Dp = 6.dp,
    val itemSpacing: Dp = 8.dp,
) {
    init {
        require(cornerRadius.value >= 0f) { "cornerRadius must be non-negative" }
        require(horizontalPadding.value >= 0f) { "horizontalPadding must be non-negative" }
        require(verticalPadding.value >= 0f) { "verticalPadding must be non-negative" }
        require(itemSpacing.value >= 0f) { "itemSpacing must be non-negative" }
    }
}
