package com.himanshoe.charty.common.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.ChartScaffoldConfig

/**
 * Theme-derived defaults for chart parameters. Each function reads the ambient [currentChartyTheme],
 * so a chart wrapped in a [ChartyThemeProvider] picks up its styling without the caller repeating it.
 * Use these as the default values of a chart's `color` and `scaffoldConfig` parameters.
 */
object ChartyThemeDefaults {
    /**
     * A [ChartScaffoldConfig] whose axis color, grid color, and label text style come from the
     * ambient [ChartyTheme].
     */
    @Composable
    fun scaffoldConfig(): ChartScaffoldConfig {
        val theme = currentChartyTheme
        return remember(theme) {
            ChartScaffoldConfig(
                axisColor = theme.axisColor,
                gridColor = theme.gridColor,
                labelTextStyle = theme.labelTextStyle,
            )
        }
    }

    /**
     * The single-series chart color from the ambient [ChartyTheme] ([ChartyTheme.primaryColor]) as a
     * solid [ChartyColor].
     */
    @Composable
    fun primaryColor(): ChartyColor = ChartyColor.Solid(currentChartyTheme.primaryColor)

    /**
     * The color for series [index] from the ambient [ChartyTheme] palette (see
     * [ChartyTheme.colorForSeries]) as a solid [ChartyColor].
     */
    @Composable
    fun seriesColor(index: Int): ChartyColor = ChartyColor.Solid(currentChartyTheme.colorForSeries(index))
}
