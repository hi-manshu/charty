package com.himanshoe.charty.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.color.withChartyColor

private const val DARK_AXIS_ARGB = 0xFFBDBDBD
private const val DARK_GRID_ARGB = 0xFF3A3A3A

private val LIGHT_PALETTE: List<ChartyColor> = ChartyColors.ModernPalette.value.map { ChartyColor.Solid(it) }

/**
 * Centralized visual defaults for every Charty chart: the series palette, the axis and grid colors,
 * and — grouped into small holders so a caller can override one area without restating the rest —
 * the [typography], [componentColors], [shapes], and [dimensions] every overlay is drawn with.
 * Charts read the theme through composition, so styling lives in one place instead of being repeated
 * on every chart's config.
 *
 * Every color is a [ChartyColor], so a gradient is accepted wherever a solid color is, text included.
 *
 * When no [ChartyThemeProvider] wraps the charts, the defaults follow the system light/dark setting
 * (see [currentChartyTheme]) so axes and labels stay visible on a dark background. Provide a
 * [ChartyThemeProvider] to override.
 *
 * The primary constructor's defaults are the **light** values; use [ChartyTheme.dark] for dark
 * defaults, or construct your own. They reproduce exactly what Charty drew before the theme covered
 * these surfaces, so adopting the theme never changes rendering on its own.
 *
 * Mapping a Material 3 design system is a few obvious lines:
 *
 * ```kotlin
 * ChartyThemeProvider(
 *     theme =
 *         ChartyTheme(
 *             palette = myBrand.series.map { ChartyColor.Solid(it) },
 *             primaryColor = ChartyColor.Solid(MaterialTheme.colorScheme.primary),
 *             axisColor = ChartyColor.Solid(MaterialTheme.colorScheme.outline),
 *             gridColor = ChartyColor.Solid(MaterialTheme.colorScheme.outlineVariant),
 *             typography =
 *                 ChartyTypography(
 *                     axisLabel = MaterialTheme.typography.labelSmall,
 *                     tooltipLabel = MaterialTheme.typography.bodySmall,
 *                 ),
 *             componentColors =
 *                 ChartyComponentColors(
 *                     tooltipBackground = ChartyColor.Solid(MaterialTheme.colorScheme.inverseSurface),
 *                     tooltipText = ChartyColor.Solid(MaterialTheme.colorScheme.inverseOnSurface),
 *                 ),
 *             shapes = ChartyShapes(tooltip = MaterialTheme.shapes.small),
 *         ),
 * ) {
 *     BarChart(data = { bars })
 * }
 * ```
 *
 * @property palette Ordered colors used for multi-series charts; [colorForSeries] cycles through it.
 * @property primaryColor Fallback single-series color, also used when [palette] is empty.
 * @property axisColor Color of the axis lines.
 * @property gridColor Color of the grid lines.
 * @property typography Font attributes for every text role Charty draws.
 * @property componentColors Colors for tooltips, crosshair, markers, annotations, selection,
 *   reference lines and bands, and the scroll-edge scrim.
 * @property labelTextStyle Resolved axis-label style; defaults to [ChartyTypography.axisLabel]
 *   painted with [ChartyComponentColors.axisLabelText], so overriding either flows through here.
 * @property shapes Corner treatments for Charty's floating surfaces.
 * @property dimensions Sizes and spacings for Charty's overlays.
 */
@Immutable
data class ChartyTheme(
    val palette: List<ChartyColor> = LIGHT_PALETTE,
    val primaryColor: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    val axisColor: ChartyColor = ChartyColor.Solid(Color.Black),
    val gridColor: ChartyColor = ChartyColor.Solid(Color.LightGray),
    val typography: ChartyTypography = ChartyTypography.default(),
    val componentColors: ChartyComponentColors = ChartyComponentColors.light(),
    val labelTextStyle: TextStyle = typography.axisLabel.withChartyColor(componentColors.axisLabelText),
    val shapes: ChartyShapes = ChartyShapes.default(),
    val dimensions: ChartyDimensions = ChartyDimensions.default(),
) {
    /**
     * The crosshair pill's fill: [ChartyComponentColors.crosshairLabelBackground] when set, otherwise
     * [primaryColor], which is the inheritance the crosshair has always used.
     */
    val crosshairLabelBackground: ChartyColor
        get() = componentColors.crosshairLabelBackground ?: primaryColor

    /**
     * The crosshair pill's text style: [ChartyTypography.crosshairLabel] painted with
     * [ChartyComponentColors.crosshairLabelText], falling back to the axis-label color when that role
     * is left unset.
     */
    val crosshairLabelStyle: TextStyle
        get() =
            typography.crosshairLabel.withChartyColor(
                componentColors.crosshairLabelText ?: componentColors.axisLabelText,
            )

    /**
     * Returns the palette color for series [index], wrapping around when there are more series than
     * palette entries. Falls back to [primaryColor] when [palette] is empty. Negative indices wrap
     * the same way.
     */
    fun colorForSeries(index: Int): ChartyColor =
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
                axisColor = ChartyColor.Solid(Color(DARK_AXIS_ARGB)),
                gridColor = ChartyColor.Solid(Color(DARK_GRID_ARGB)),
                componentColors = ChartyComponentColors.dark(),
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
