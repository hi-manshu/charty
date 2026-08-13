package com.himanshoe.charty.common.theme

import androidx.compose.runtime.Composable
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig

/**
 * Resolves a chart's optional [TooltipConfig] against the ambient [ChartyTheme].
 *
 * A config is a plain data class, so its property defaults cannot read a composition local. Chart
 * configs therefore declare their tooltip as `null` — meaning "whatever the theme says" — and each
 * chart calls this from composition to turn that into the config it actually draws with. Passing an
 * explicit [TooltipConfig] keeps it verbatim, so a caller always wins over the theme.
 *
 * @receiver The tooltip config the caller supplied, or `null` to take the theme's.
 * @param showArrow Whether the themed bubble points an arrow at its anchor. Ignored when the caller
 *   supplied a config of their own.
 */
@Composable
internal fun TooltipConfig?.orThemeDefault(showArrow: Boolean = true): TooltipConfig =
    this ?: ChartyThemeDefaults.tooltipConfig(showArrow = showArrow)

/**
 * Resolves a chart's optional [ChartCrosshairConfig] against the ambient [ChartyTheme], on the same
 * terms as [orThemeDefault]: `null` takes the theme's guide lines, dot, and value bubble, and an
 * explicit config is kept verbatim.
 *
 * @receiver The crosshair config the caller supplied, or `null` to take the theme's.
 */
@Composable
internal fun ChartCrosshairConfig?.orThemeCrosshair(): ChartCrosshairConfig =
    this ?: ChartyThemeDefaults.crosshairConfig()
