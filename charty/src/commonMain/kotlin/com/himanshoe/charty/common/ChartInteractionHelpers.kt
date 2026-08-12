package com.himanshoe.charty.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.common.annotation.drawChartAnnotation
import com.himanshoe.charty.common.brush.BrushSelectionState
import com.himanshoe.charty.common.brush.drawBrushSelection
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.downsample.lttbDownsample
import com.himanshoe.charty.common.draw.drawScrollEdgeFades
import com.himanshoe.charty.common.gesture.chartBrushSelectionHandler
import com.himanshoe.charty.common.gesture.chartStreamingPan
import com.himanshoe.charty.common.gesture.chartZoomAndPan
import com.himanshoe.charty.common.streaming.StreamingState
import com.himanshoe.charty.common.viewport.ViewPortState

/**
 * Resolves the windowed/visible slice of [fullDataList] and, when the rolling [visibleWindow] is
 * active, the [StreamingLayout] that slides it. Precedence: an interactive [viewPortState] (zoom/pan)
 * wins over [visibleWindow]; with a viewport set the chart draws statically. When [visibleWindow] is
 * set and no viewport is configured the chart streams — the window slides on append (see
 * [rememberVisibleWindowSlice]) using [animation]. When both are null, returns [fullDataList] as-is.
 * Every hook path runs on every composition, so switching modes never changes the hook order.
 */
@Composable
internal fun <T> rememberWindowedData(
    fullDataList: List<T>,
    viewPortState: ViewPortState?,
    visibleWindow: Int?,
    animation: Animation,
    streamingState: StreamingState? = null,
): VisibleChartData<T> {
    val streamingActive = visibleWindow != null && viewPortState == null && fullDataList.size > 1
    val (streamData, streamLayout) =
        rememberVisibleWindowSlice(
            fullDataList = fullDataList,
            windowSize = visibleWindow ?: 1,
            animation = if (streamingActive) animation else Animation.Disabled,
            streamingState = if (streamingActive) streamingState else null,
        )
    val staticWindow =
        remember(fullDataList, viewPortState?.startFraction, viewPortState?.endFraction, visibleWindow) {
            if (viewPortState == null) {
                tailWindow(fullDataList, visibleWindow)
            } else {
                val range = viewPortState.visibleIndices(fullDataList.size)
                fullDataList.subList(range.first, range.last + 1)
            }
        }
    return if (streamingActive) {
        VisibleChartData(data = streamData, streaming = streamLayout)
    } else {
        VisibleChartData(data = staticWindow, streaming = null)
    }
}

/**
 * Remembers an LTTB-downsampled view of [dataList] when it exceeds [threshold] points, so charts stay
 * at interactive frame rates on large series. When [threshold] is `null` or the list is already within
 * budget, [dataList] is returned unchanged. See [lttbDownsample].
 *
 * @param dataList The (already windowed) points to draw.
 * @param threshold Maximum points to render; `null` disables downsampling.
 * @param value Extracts the y-value used to preserve the line's shape.
 */
@Composable
internal fun <T> rememberDownsampledData(
    dataList: List<T>,
    threshold: Int?,
    value: (T) -> Float,
): List<T> =
    remember(dataList, threshold) {
        if (threshold == null || dataList.size <= threshold) {
            dataList
        } else {
            lttbDownsample(data = dataList, threshold = threshold, value = value)
        }
    }

/**
 * Returns the last [visibleWindow] items of [dataList] (a rolling "show last N" window), or
 * [dataList] unchanged when [visibleWindow] is `null` or the list is already within the window.
 */
internal fun <T> tailWindow(
    dataList: List<T>,
    visibleWindow: Int?,
): List<T> =
    if (visibleWindow == null || dataList.size <= visibleWindow) {
        dataList
    } else {
        dataList.takeLast(visibleWindow)
    }

/**
 * The single windowing entry point for value-series charts: the viewport/rolling/static window of
 * [fullDataList] (via [rememberWindowedData]) with an LTTB downsample to [downsampleThreshold] layered
 * on top when the chart is not streaming. Returns the points to draw plus the optional
 * [StreamingLayout] that slides them, so hit-testing and drawing stay on the exact same list.
 *
 * Downsampling is skipped while streaming: the rolling window is already small, and the layout indexes
 * the window slice directly, so thinning it would break the position mapping.
 *
 * @param fullDataList The complete series.
 * @param interactionConfig Supplies the viewport (zoom/pan) state, which takes precedence over [visibleWindow].
 * @param downsampleThreshold Maximum points to render in the non-streaming path; `null` disables downsampling.
 * @param visibleWindow Rolling "show last N" window; `null` shows everything. Ignored when a viewport is set.
 * @param animation Drives the streaming slide's duration/easing; a disabled animation snaps (instant advance).
 * @param value Extracts the y-value used to preserve the line's shape when downsampling.
 */
@Composable
internal fun <T> rememberVisibleData(
    fullDataList: List<T>,
    interactionConfig: ChartInteractionConfig,
    downsampleThreshold: Int?,
    visibleWindow: Int?,
    animation: Animation,
    value: (T) -> Float,
): VisibleChartData<T> {
    val windowed =
        rememberWindowedData(
            fullDataList = fullDataList,
            viewPortState = interactionConfig.viewPortState,
            visibleWindow = visibleWindow,
            animation = animation,
            streamingState = interactionConfig.streamingState,
        )
    val downsampled = rememberDownsampledData(dataList = windowed.data, threshold = downsampleThreshold, value = value)
    val data = if (windowed.streaming != null) windowed.data else downsampled
    return VisibleChartData(data = data, streaming = windowed.streaming)
}

/**
 * Remembers an auto-generated or caller-supplied chart accessibility description.
 * Returns null when [accessibilityDescription] is an empty string (suppressed).
 */
@Composable
internal fun <T> rememberChartDescription(
    data: List<T>,
    accessibilityDescription: String?,
    generator: (List<T>) -> String,
): String? =
    remember(data, accessibilityDescription) {
        when (accessibilityDescription) {
            "" -> null
            null -> generator(data)
            else -> accessibilityDescription
        }
    }

/** Propagates the current data-list sizes into the interaction state holders. */
internal fun syncInteractionDataSizes(
    viewPortState: ViewPortState?,
    brushSelectionState: BrushSelectionState?,
    fullDataSize: Int,
    dataSize: Int,
) {
    viewPortState?.let { it.dataSize = fullDataSize }
    brushSelectionState?.let { it.dataSize = dataSize }
}

/**
 * `true` when drag-to-track tooltips should be active: the caller opted in via
 * [ChartInteractionConfig.dragTooltipEnabled] and no other drag-consuming interaction (zoom/pan or
 * brush selection) is configured.
 */
internal val ChartInteractionConfig.dragTooltipActive: Boolean
    get() = dragTooltipEnabled && viewPortState == null && brushSelectionState == null

/** Builds brush-selection and zoom-pan modifiers and chains them onto [base]. */
internal fun <T> buildInteractionModifier(
    base: Modifier,
    interactionConfig: ChartInteractionConfig,
    dataList: List<T>,
): Modifier {
    val brushModifier =
        if (interactionConfig.brushSelectionState != null) {
            Modifier.chartBrushSelectionHandler(
                dataList = dataList,
                brushState = interactionConfig.brushSelectionState,
                onRangeSelect = interactionConfig.onRangeSelect,
            )
        } else {
            Modifier
        }
    val zoomModifier =
        if (interactionConfig.viewPortState != null) {
            Modifier.chartZoomAndPan(interactionConfig.viewPortState)
        } else {
            Modifier
        }
    return base.then(brushModifier).then(zoomModifier)
}

/**
 * The scrollback drag modifier for a streaming chart, or an empty [Modifier] when the chart is not
 * streaming with scrollback enabled — which is every chart without both a rolling `visibleWindow` and a
 * [ChartInteractionConfig.streamingState], so no existing chart gains a gesture it did not have before.
 *
 * Chain it last so it sees pointer events before the tap handlers underneath it: it only consumes them
 * once the drag passes touch slop, leaving taps to the tooltip.
 *
 * @param streaming The active streaming layout, which reports the visible window size.
 * @param orientation The chart's orientation, deciding the axis the drag is measured along.
 */
internal fun ChartInteractionConfig.streamingPan(
    streaming: StreamingLayout?,
    orientation: ChartOrientation = ChartOrientation.VERTICAL,
): Modifier {
    val state = streamingState
    if (streaming == null || state == null) {
        return Modifier
    }
    return Modifier.chartStreamingPan(
        state = state,
        orientation = orientation,
        windowSize = streaming.windowSize,
    )
}

/** Updates the chart-context bounds in the viewport and brush-selection state holders. */
internal fun updateInteractionBounds(
    interactionConfig: ChartInteractionConfig,
    chartContext: ChartContext,
) {
    interactionConfig.viewPortState?.let { vp ->
        vp.chartLeft = chartContext.left
        vp.chartWidth = chartContext.width
    }
    interactionConfig.brushSelectionState?.let { bs ->
        bs.chartLeft = chartContext.left
        bs.chartRight = chartContext.right
    }
    interactionConfig.streamingState?.updatePlotBounds(left = chartContext.left, width = chartContext.width)
}

/** Draws annotation markers and the brush-selection overlay as the last canvas pass. */
internal fun DrawScope.drawInteractionOverlays(
    interactionConfig: ChartInteractionConfig,
    chartContext: ChartContext,
    totalItems: Int,
    textMeasurer: TextMeasurer,
) {
    interactionConfig.annotations.fastForEach { annotation ->
        drawChartAnnotation(
            annotation = annotation,
            chartContext = chartContext,
            totalItems = totalItems,
            textMeasurer = textMeasurer,
        )
    }
    interactionConfig.brushSelectionState?.let { bs ->
        drawBrushSelection(
            state = bs,
            chartLeft = chartContext.left,
            chartTop = chartContext.top,
            chartRight = chartContext.right,
            chartBottom = chartContext.bottom,
        )
    }
    val viewPortState = interactionConfig.viewPortState
    interactionConfig.edgeFade?.let { edgeFade ->
        if (viewPortState != null) {
            drawScrollEdgeFades(
                chartContext = chartContext,
                config = edgeFade,
                fadeLeft = !viewPortState.isAtStart,
                fadeRight = !viewPortState.isAtEnd,
            )
        }
    }
}

/**
 * Follows the end of the data when [enabled] and a [viewPortState] is present: each time
 * [fullDataSize] grows the viewport animates to reveal the newest points (see
 * [ViewPortState.animateScrollToEnd]). Call from a chart's composition.
 */
@Composable
internal fun AutoScrollToLatestEffect(
    viewPortState: ViewPortState?,
    fullDataSize: Int,
    enabled: Boolean,
) {
    LaunchedEffect(viewPortState, fullDataSize, enabled) {
        if (enabled && viewPortState != null) {
            viewPortState.animateScrollToEnd()
        }
    }
}
