package com.himanshoe.charty.line.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.theme.orThemeCrosshair
import com.himanshoe.charty.common.theme.orThemeDefault
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.line.config.LineChartConfig

/**
 * A line-family chart's styling once the ambient theme has been folded in: the config the chart
 * draws with, and the tooltip styling its draw pass needs as a non-null value.
 *
 * @property config The caller's [LineChartConfig], carrying the resolved crosshair styling when the
 *   chart was given a crosshair.
 * @property tooltipConfig The tooltip styling to draw with, never `null`.
 * @property activeCrosshair The crosshair the chart should run, whether it came from the chart's own
 *   `crosshair` parameter or from [LineChartConfig.crosshairConfig]; `null` when the chart has none.
 */
@Immutable
internal class ThemedLineStyling<T>(
    val config: LineChartConfig,
    val tooltipConfig: TooltipConfig,
    val activeCrosshair: ChartCrosshair<T>?,
)

/**
 * Resolves the theme-driven parts of a line-family chart's styling in one step, so the four charts
 * that share [LineChartConfig] resolve them identically.
 *
 * A `null` crosshair config means "take the theme's", and a chart given no [crosshair] at all keeps
 * its config untouched so a crosshair configured through [LineChartConfig.crosshairConfig] still
 * decides on its own whether the crosshair is on.
 *
 * @param lineConfig The config the caller passed to the chart.
 * @param crosshair The crosshair the caller passed to the chart, or `null` when they passed none.
 */
@Composable
internal fun <T> rememberThemedLineStyling(
    lineConfig: LineChartConfig,
    crosshair: ChartCrosshair<T>?,
): ThemedLineStyling<T> {
    val themedCrosshairConfig = crosshair?.config.orThemeCrosshair()
    val tooltipConfig = lineConfig.tooltipConfig.orThemeDefault()
    return ThemedLineStyling(
        config = crosshair?.let { lineConfig.copy(crosshairConfig = themedCrosshairConfig) } ?: lineConfig,
        tooltipConfig = tooltipConfig,
        activeCrosshair = crosshair ?: lineConfig.crosshairConfig?.let { ChartCrosshair(config = it) },
    )
}
