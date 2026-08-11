package com.himanshoe.charty.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.theme.currentChartyTheme

private const val EMPTY_TEXT_SP = 14

/**
 * The placeholder a chart renders instead of throwing when it has no data — so empty or
 * not-yet-loaded data shows a message rather than crashing. Charts fall back to this automatically
 * when their data list is empty; it is public so callers can reuse the same look.
 *
 * @param modifier Modifier applied to the placeholder (typically the chart's own modifier, so it
 *   fills the same space).
 * @param message The text shown; defaults to "No data".
 * @param textStyle Text style for the message; defaults to the ambient theme's label color.
 */
@Composable
fun ChartEmptyState(
    modifier: Modifier = Modifier,
    message: String = "No data",
    textStyle: TextStyle = TextStyle(color = currentChartyTheme.labelTextStyle.color, fontSize = EMPTY_TEXT_SP.sp),
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        BasicText(text = message, style = textStyle)
    }
}
