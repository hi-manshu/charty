package com.himanshoe.charty.common.tooltip

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders the Compose-overlay side of a [ChartTooltip]. Place it as a sibling over the chart canvas.
 * Does nothing for [ChartTooltip.canvas]/[ChartTooltip.none] (canvas tooltips are drawn inside the
 * chart's own canvas) or when there is no active selection.
 *
 * @param tooltip The tooltip configuration chosen by the caller.
 * @param item The currently selected data point, or `null` when nothing is selected.
 * @param anchor The tooltip anchor for the selection, or `null` when nothing is selected.
 * @param modifier Modifier for the overlay container (typically `Modifier.matchParentSize()`).
 */
@Composable
fun <T> ChartTooltipHost(
    tooltip: ChartTooltip<T>,
    item: T?,
    anchor: TooltipState?,
    modifier: Modifier = Modifier,
) {
    val compose = tooltip as? ComposeTooltip<T> ?: return
    if (item == null || anchor == null) {
        return
    }
    ChartTooltipOverlay(
        item = item,
        anchor = anchor,
        config = compose.config,
        modifier = modifier,
    ) { data ->
        RenderTooltipContent(
            scope = TooltipScope(data = data, text = anchor.content, anchor = anchor),
            content = compose.content,
        )
    }
}

@Composable
private fun <T> RenderTooltipContent(
    scope: TooltipScope<T>,
    content: @Composable TooltipScope<T>.() -> Unit,
) {
    scope.content()
}

/** Whether [tooltip] draws its bubble on the chart canvas (so the chart should render it there). */
fun ChartTooltip<*>.isCanvas(): Boolean = this is CanvasTooltip
