package com.himanshoe.charty.common.tooltip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.theme.currentChartyTheme

private const val CARD_TEXT_ARGB = 0xFF212121
private const val PILL_CORNER_RADIUS_DP = 999

/**
 * A compact rounded pill tooltip showing the point's [TooltipScope.text]. Background defaults to the
 * ambient [com.himanshoe.charty.common.theme.ChartyTheme] primary color.
 *
 * @param backgroundColor Pill fill as a [ChartyColor] (solid or gradient).
 * @param textStyle Text style for the label.
 * @param cornerRadius Corner radius of the pill.
 * @param contentPadding Padding between text and the pill edges.
 */
@Composable
fun TooltipScope<*>.PillTooltip(
    modifier: Modifier = Modifier,
    backgroundColor: ChartyColor = currentChartyTheme.primaryColor,
    textStyle: TextStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    cornerRadius: Int = PILL_CORNER_RADIUS_DP,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
) {
    Box(
        modifier =
            modifier
                .background(brush = backgroundColor.toTooltipBrush(), shape = RoundedCornerShape(cornerRadius.dp))
                .padding(contentPadding),
    ) {
        BasicText(text = text, style = textStyle)
    }
}

/**
 * A slightly larger elevated-looking card tooltip with a colored series swatch and the point's
 * [TooltipScope.text].
 *
 * @param accent Swatch/border accent as a [ChartyColor].
 * @param backgroundColor Card fill as a [ChartyColor].
 * @param textStyle Text style for the label.
 */
@Composable
fun TooltipScope<*>.CardTooltip(
    modifier: Modifier = Modifier,
    accent: ChartyColor = currentChartyTheme.primaryColor,
    backgroundColor: ChartyColor = ChartyColor.Solid(Color.White),
    textStyle: TextStyle = TextStyle(color = Color(CARD_TEXT_ARGB), fontSize = 12.sp, fontWeight = FontWeight.Medium),
) {
    Box(
        modifier =
            modifier
                .background(brush = accent.toTooltipBrush(), shape = RoundedCornerShape(10.dp))
                .padding(start = 4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .background(
                        brush = backgroundColor.toTooltipBrush(),
                        shape =
                            RoundedCornerShape(
                                topEnd = 10.dp,
                                bottomEnd = 10.dp,
                                topStart = 0.dp,
                                bottomStart = 0.dp,
                            ),
                    ).padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            BasicText(text = text, style = textStyle)
        }
    }
}

internal fun ChartyColor.toTooltipBrush(): Brush =
    when (this) {
        is ChartyColor.Solid -> SolidColor(color)
        is ChartyColor.Gradient -> Brush.verticalGradient(colors)
    }
