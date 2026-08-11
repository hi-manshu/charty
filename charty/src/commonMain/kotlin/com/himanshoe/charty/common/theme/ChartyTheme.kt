package com.himanshoe.charty.common.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColors

private val DEFAULT_LABEL_TEXT_STYLE = TextStyle(color = Color.Black, fontSize = 12.sp)

/**
 * Centralized visual defaults for every Charty chart — the series palette, axis and grid colors, and
 * the axis-label text style. Provide one with [ChartyThemeProvider] and the charts nested under it
 * pick it up through composition, so styling lives in one place instead of being repeated on every
 * chart's config.
 *
 * The default values match Charty's built-in look, so wrapping (or not wrapping) charts in a theme
 * changes nothing until you supply your own.
 *
 * @property palette Ordered colors used for multi-series charts; [colorForSeries] cycles through it.
 * @property primaryColor Fallback single-series color, also used when [palette] is empty.
 * @property axisColor Color of the axis lines.
 * @property gridColor Color of the grid lines.
 * @property labelTextStyle Text style for axis labels.
 */
@Immutable
data class ChartyTheme(
    val palette: List<Color> = ChartyColors.ModernPalette.value,
    val primaryColor: Color = ChartyColors.Blue,
    val axisColor: Color = Color.Black,
    val gridColor: Color = Color.LightGray,
    val labelTextStyle: TextStyle = DEFAULT_LABEL_TEXT_STYLE,
) {
    /**
     * Returns the palette color for series [index], wrapping around when there are more series than
     * palette entries. Falls back to [primaryColor] when [palette] is empty. Negative indices wrap
     * the same way.
     */
    fun colorForSeries(index: Int): Color =
        if (palette.isEmpty()) {
            primaryColor
        } else {
            palette[index.mod(palette.size)]
        }
}

/**
 * The [ChartyTheme] in scope. Defaults to [ChartyTheme] with Charty's built-in values when no
 * [ChartyThemeProvider] wraps the caller.
 */
val LocalChartyTheme = staticCompositionLocalOf { ChartyTheme() }

/**
 * The [ChartyTheme] currently in scope, read from [LocalChartyTheme].
 */
val currentChartyTheme: ChartyTheme
    @Composable
    @ReadOnlyComposable
    get() = LocalChartyTheme.current

/**
 * Provides [theme] to every Charty chart in [content]. Charts read their default palette, axis, grid,
 * and label styling from the nearest provider.
 *
 * @param theme The theme to apply.
 * @param content The chart content that should use [theme].
 */
@Composable
fun ChartyThemeProvider(
    theme: ChartyTheme = ChartyTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalChartyTheme provides theme, content = content)
}
