package com.himanshoe.charty.common.tooltip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * How a chart shows the tooltip for the selected point. Create one with the factory functions and
 * pass it to a chart's `tooltip` parameter — the caller decides between the built-in canvas tooltip,
 * a fully custom Composable overlay, or none:
 *
 * ```kotlin
 * tooltip = ChartTooltip.canvas()                 // built-in, styled via the chart's TooltipConfig
 * tooltip = ChartTooltip.compose { PillTooltip() } // your Composable, positioned for you
 * tooltip = ChartTooltip.compose { scope ->        // fully custom layout with access to the data
 *     MyCard(title = scope.text, value = scope.data.value)
 * }
 * tooltip = ChartTooltip.none()                    // no tooltip
 * ```
 *
 * @param T The chart's data-point type (e.g. `LineData`).
 */
@Immutable
sealed interface ChartTooltip<T> {
    /** Factories for the available tooltip strategies: [none], [canvas], and [compose]. */
    companion object {
        /** No tooltip is shown. */
        fun <T> none(): ChartTooltip<T> = NoneTooltip()

        /**
         * The built-in tooltip drawn on the chart canvas — fast and always inside the plot. Style it
         * through the chart's `TooltipConfig` (shape, colors, arrow, text style).
         */
        fun <T> canvas(): ChartTooltip<T> = CanvasTooltip()

        /**
         * A Compose overlay tooltip: [content] is any Composable, positioned over the selected point
         * with edge-collision handling. Use a preset (e.g. `PillTooltip`) or build your own from the
         * [TooltipScope].
         *
         * @param config Positioning/appearance hints (offset, edge margin) shared with the overlay.
         * @param content The tooltip UI, with the selected point available via [TooltipScope].
         */
        fun <T> compose(
            config: TooltipConfig = TooltipConfig(),
            content: @Composable TooltipScope<T>.() -> Unit,
        ): ChartTooltip<T> = ComposeTooltip(config = config, content = content)
    }
}

internal class NoneTooltip<T> : ChartTooltip<T>

internal class CanvasTooltip<T> : ChartTooltip<T>

internal class ComposeTooltip<T>(
    val config: TooltipConfig,
    val content: @Composable TooltipScope<T>.() -> Unit,
) : ChartTooltip<T>

/**
 * The context a [ChartTooltip.compose] tooltip is rendered in: the selected [data] point, the
 * chart's formatted [text] for it, and the [anchor] position/size on the canvas.
 *
 * @property data The selected data point.
 * @property text The chart's formatted label for [data] (from its tooltip formatter).
 * @property anchor The tooltip anchor (canvas position and bar width) for the selection.
 */
@Stable
class TooltipScope<T> internal constructor(
    val data: T,
    val text: String,
    val anchor: TooltipState,
)
