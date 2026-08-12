package com.himanshoe.charty.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.streaming.StreamingState

/**
 * Everything [ChartScaffold] needs to render a chart in rolling-window streaming mode: where the window
 * currently sits, and the floating control that offers to jump back to the live edge.
 *
 * The two travel together because they are two views of the same thing — a window that has been dragged
 * off the newest point — and bundling them keeps the scaffold's parameter list stable as streaming grows
 * new affordances.
 *
 * Instances are created by the charts themselves; pass `null` to [ChartScaffold] for a static chart.
 *
 * @property layout The sliding window layout that positions the series and the category-axis labels, or
 *   `null` when the chart is not streaming.
 * @property overlay Composable drawn on top of the plot while the window is detached, typically a
 *   "jump to latest" affordance. `null` renders nothing.
 */
@Immutable
class ChartStreamingRender internal constructor(
    val layout: StreamingLayout?,
    val overlay: (@Composable () -> Unit)? = null,
)

/**
 * Builds the [ChartStreamingRender] a chart hands to [ChartScaffold], wiring the consumer's
 * [ChartInteractionConfig.jumpToLatest] slot to [ChartInteractionConfig.streamingState] only when the
 * chart is actually streaming with scrollback enabled. Anything less — no rolling window, no scrollback
 * state, or no overlay slot — renders the layout alone, exactly as before.
 *
 * @param layout The active streaming layout, or `null` when the chart is not streaming.
 */
internal fun ChartInteractionConfig.streamingRender(layout: StreamingLayout?): ChartStreamingRender {
    val state = streamingState
    val slot = jumpToLatest
    if (layout == null || state == null || slot == null) {
        return ChartStreamingRender(layout = layout, overlay = null)
    }
    return ChartStreamingRender(
        layout = layout,
        overlay = { ChartJumpToLatestSlot(state = state, content = slot) },
    )
}

/**
 * Renders [content] only while [state] is detached from the live edge, so the "jump to latest" control
 * appears exactly when there is somewhere to jump to. Keeping the check in its own composable scopes the
 * [StreamingState.isFollowing] read here instead of recomposing the whole chart on every attach/detach.
 *
 * @param state The scrollback state being observed.
 * @param content The consumer-supplied control.
 */
@Composable
internal fun ChartJumpToLatestSlot(
    state: StreamingState,
    content: @Composable (StreamingState) -> Unit,
) {
    if (!state.isFollowing) {
        content(state)
    }
}
