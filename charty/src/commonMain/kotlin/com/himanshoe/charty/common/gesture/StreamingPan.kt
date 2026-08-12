package com.himanshoe.charty.common.gesture

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.streaming.StreamingState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Converts a drag measured in pixels into a scroll delta measured in data indices.
 *
 * One category slot is `plotExtentPixels / windowSize` wide, so dragging by exactly one slot moves the
 * window by exactly one index. The result is negated because the plot is dragged like a sheet of paper
 * under the finger: pulling the content towards the trailing edge (right on a vertical chart, down on a
 * horizontal one) reveals what came *before*, which is a **decreasing** scroll position — the same
 * direction convention every touch-scrollable surface uses.
 *
 * Degenerate inputs — a window of zero or fewer slots, or a plot that has not been measured yet — yield
 * `0f` rather than an infinite or NaN delta, so a gesture arriving before the first layout is a no-op.
 *
 * @param dragPixels Signed pixel movement along the category axis since the previous pointer event.
 * @param plotExtentPixels Length of the category axis in pixels (width for vertical charts, height for
 *   horizontal ones).
 * @param windowSize Number of category slots visible across the plot.
 * @return The signed number of data indices to add to the current scroll position.
 */
internal fun panDeltaToIndexDelta(
    dragPixels: Float,
    plotExtentPixels: Float,
    windowSize: Int,
): Float {
    if (windowSize <= 0 || plotExtentPixels <= 0f) {
        return 0f
    }
    return -dragPixels * windowSize / plotExtentPixels
}

/**
 * Lets the reader drag a streaming chart backwards through history.
 *
 * `detectDragGestures` only reports movement once the pointer has travelled past touch slop, so a plain
 * tap still reaches the chart's tooltip handler untouched. From then on every movement is translated
 * into a data index delta by [panDeltaToIndexDelta] and applied through [StreamingState.scrollBy], which
 * detaches the window from the live edge and clamps the position into `0..maxScroll`.
 *
 * `scrollBy` suspends while the drag callback does not, so deltas cross into a single long-lived
 * coroutine through an unbounded channel. That keeps them strictly ordered and lossless — one coroutine
 * launched per pointer event would let the animation mutex apply them out of order, which is exactly the
 * jitter this gesture must not have.
 *
 * Each delta snaps the scroll to its new position instead of animating towards it: the finger is already
 * the animation, and tweening on top of it is what makes a drag feel rubbery. There is no fling — a chart
 * being read backwards wants to stop where it was released.
 *
 * The category axis follows the chart's [orientation]: vertical charts pan along X, horizontal charts
 * along Y. Its extent is taken from the pointer input layout size, which includes the axis gutters; the
 * resulting sub-slot difference is imperceptible in a drag and keeps the gesture independent of the
 * draw pass.
 *
 * @param state The streaming state that owns the scroll position.
 * @param orientation The chart's orientation, which decides the axis the drag is measured along.
 * @param windowSize Number of category slots visible across the plot.
 */
internal fun Modifier.chartStreamingPan(
    state: StreamingState,
    orientation: ChartOrientation,
    windowSize: Int,
): Modifier =
    this.pointerInput(state, orientation, windowSize) {
        coroutineScope {
            val deltas = Channel<Float>(capacity = Channel.UNLIMITED)
            launch {
                for (delta in deltas) {
                    state.scrollBy(delta)
                }
            }
            detectDragGestures { change, dragAmount ->
                change.consume()
                deltas.trySend(
                    panDeltaToIndexDelta(
                        dragPixels = axisDelta(offset = dragAmount, orientation = orientation),
                        plotExtentPixels = categoryExtent(size = size, orientation = orientation),
                        windowSize = windowSize,
                    ),
                )
            }
        }
    }

/**
 * Length of the category axis within a pointer input area of [size], read per event so a resize takes
 * effect without restarting the gesture.
 */
private fun categoryExtent(
    size: IntSize,
    orientation: ChartOrientation,
): Float =
    if (orientation == ChartOrientation.HORIZONTAL) {
        size.height.toFloat()
    } else {
        size.width.toFloat()
    }

/** Projects [offset] onto the category axis of a chart drawn with [orientation]. */
private fun axisDelta(
    offset: Offset,
    orientation: ChartOrientation,
): Float =
    if (orientation == ChartOrientation.HORIZONTAL) {
        offset.y
    } else {
        offset.x
    }
