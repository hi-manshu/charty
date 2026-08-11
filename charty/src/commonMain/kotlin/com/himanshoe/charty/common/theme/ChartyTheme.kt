package com.himanshoe.charty.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColors

private const val LABEL_FONT_SP = 12
private const val DARK_AXIS_ARGB = 0xFFBDBDBD
private const val DARK_GRID_ARGB = 0xFF3A3A3A
private const val DARK_LABEL_ARGB = 0xFFE0E0E0

private val LIGHT_LABEL_TEXT_STYLE = TextStyle(color = Color.Black, fontSize = LABEL_FONT_SP.sp)
private val DARK_LABEL_TEXT_STYLE = TextStyle(color = Color(DARK_LABEL_ARGB), fontSize = LABEL_FONT_SP.sp)

/**
 * Centralized visual defaults for every Charty chart — the series palette, axis and grid colors, and
 * the axis-label text style. Charts read it through composition, so styling lives in one place instead
 * of being repeated on every chart's config.
 *
 * When no [ChartyThemeProvider] wraps the charts, the defaults follow the system light/dark setting
 * (see [currentChartyTheme]) so axes and labels stay visible on a dark background. Provide a
 * [ChartyThemeProvider] to override.
 *
 * The primary constructor's defaults are the **light** values; use [ChartyTheme.dark] for dark
 * defaults, or construct your own.
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
    val labelTextStyle: TextStyle = LIGHT_LABEL_TEXT_STYLE,
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

    /** Light and dark preset themes. */
    companion object {
        /** The light-mode defaults (dark axis and labels on a light background). */
        fun light(): ChartyTheme = ChartyTheme()

        /** The dark-mode defaults (light axis and labels, dim grid) for a dark background. */
        fun dark(): ChartyTheme =
            ChartyTheme(
                axisColor = Color(DARK_AXIS_ARGB),
                gridColor = Color(DARK_GRID_ARGB),
                labelTextStyle = DARK_LABEL_TEXT_STYLE,
            )
    }
}

/**
 * The theme set by the nearest [ChartyThemeProvider], or `null` when none is present (in which case
 * charts fall back to the system-resolved default — see [currentChartyTheme]).
 */
val LocalChartyTheme = staticCompositionLocalOf<ChartyTheme?> { null }

/**
 * The [ChartyTheme] currently in effect: the one from the nearest [ChartyThemeProvider], or — when no
 * provider is present — [ChartyTheme.dark] in system dark mode and [ChartyTheme.light] otherwise.
 */
val currentChartyTheme: ChartyTheme
    @Composable
    @ReadOnlyComposable
    get() =
        LocalChartyTheme.current ?: if (isSystemInDarkTheme()) {
            ChartyTheme.dark()
        } else {
            ChartyTheme.light()
        }

/**
 * Provides [theme] to every Charty chart in [content]. Charts read their default palette, axis, grid,
 * and label styling from the nearest provider. Defaults to the system-resolved theme, so wrapping
 * content without an explicit [theme] simply pins the current light/dark choice.
 *
 * @param theme The theme to apply.
 * @param content The chart content that should use [theme].
 */
@Composable
fun ChartyThemeProvider(
    theme: ChartyTheme = currentChartyTheme,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalChartyTheme provides theme, content = content)
}
