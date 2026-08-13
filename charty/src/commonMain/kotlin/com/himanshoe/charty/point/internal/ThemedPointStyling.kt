package com.himanshoe.charty.point.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.theme.orThemeCrosshair
import com.himanshoe.charty.common.theme.orThemeDefault
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.point.config.PointChartConfig

/**
 * A point-family chart's styling once the ambient theme has been folded in.
 *
 * @property tooltipConfig The tooltip styling to draw with, never `null`.
 * @property crosshairConfig The crosshair styling to draw with, or `null` when the chart has no
 *   crosshair at all.
 * @property activeCrosshair The crosshair the chart should run, whether it came from the chart's own
 *   `crosshair` parameter or from [PointChartConfig.crosshairConfig]; `null` when it has none.
 */
@Immutable
internal class ThemedPointStyling<T>(
    val tooltipConfig: TooltipConfig,
    val crosshairConfig: ChartCrosshairConfig?,
    val activeCrosshair: ChartCrosshair<T>?,
)

/**
 * Resolves the theme-driven parts of a point-family chart's styling in one step, so the charts that
 * share [PointChartConfig] resolve them identically.
 *
 * A crosshair passed with no config of its own takes the theme's; a chart given no [crosshair] at all
 * falls back to [PointChartConfig.crosshairConfig], where `null` still means the crosshair is off.
 *
 * @param pointConfig The config the caller passed to the chart.
 * @param crosshair The crosshair the caller passed to the chart, or `null` when they passed none.
 */
@Composable
internal fun <T> rememberThemedPointStyling(
    pointConfig: PointChartConfig,
    crosshair: ChartCrosshair<T>?,
): ThemedPointStyling<T> {
    val themedCrosshairConfig = crosshair?.config.orThemeCrosshair()
    return ThemedPointStyling(
        tooltipConfig = pointConfig.tooltipConfig.orThemeDefault(),
        crosshairConfig = if (crosshair != null) themedCrosshairConfig else pointConfig.crosshairConfig,
        activeCrosshair = crosshair ?: pointConfig.crosshairConfig?.let { ChartCrosshair(config = it) },
    )
}
