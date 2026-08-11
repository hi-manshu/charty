package com.himanshoe.charty.common.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.common.brush.BrushSelectionState
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.viewport.ViewPortState

private val RectangularTapHitSlop = 8.dp

/**
 * Adds tap gesture detection for charts with rectangular hit areas, such as bar charts.
 *
 * @param T The type of data associated with each bound.
 * @param D The type of the items in the data list.
 * @param dataList The list of data items, used as a recomposition key.
 * @param bounds Pairs of [Rect] bounds and their associated data.
 * @param onItemClick Invoked when a bound is tapped.
 * @param onTooltipStateChange Callback to update the tooltip state.
 * @param createTooltipContent Builds a [TooltipState] from the tapped data and its bounds.
 */
fun <T, D> Modifier.rectangularChartClickHandler(
    dataList: List<D>,
    bounds: List<Pair<Rect, T>>,
    onItemClick: ((T) -> Unit)?,
    onTooltipStateChange: (TooltipState?, T?) -> Unit,
    createTooltipContent: (T, Rect) -> TooltipState,
): Modifier =
    if (onItemClick != null) {
        this.pointerInput(dataList, onItemClick) {
            val hitSlop = RectangularTapHitSlop.toPx()
            detectTapGestures { offset ->
                val clickedItem = findClickedItemWithBounds(offset, bounds, hitSlop)
                clickedItem?.let { (rect, data) ->
                    onItemClick.invoke(data)
                    onTooltipStateChange(createTooltipContent(data, rect), data)
                } ?: run {
                    onTooltipStateChange(null, null)
                }
            }
        }
    } else {
        this
    }

/**
 * Adds tap gesture detection for point-based charts, such as line or scatter charts.
 *
 * @param T The type of data associated with each point.
 * @param D The type of the items in the data list.
 * @param dataList The list of data items, used as a recomposition key.
 * @param pointBounds Pairs of [Offset] positions and their associated data.
 * @param tapRadius Maximum distance from a point that counts as a hit.
 * @param onPointClick Invoked when a point is tapped.
 * @param onTooltipStateChange Callback to update the tooltip state.
 * @param createTooltipContent Builds a [TooltipState] from the tapped data and position.
 */
fun <T, D> Modifier.pointChartClickHandler(
    dataList: List<D>,
    pointBounds: List<Pair<Offset, T>>,
    tapRadius: Float,
    onPointClick: (T) -> Unit,
    onTooltipStateChange: (TooltipState?, T?) -> Unit,
    createTooltipContent: (T, Offset) -> TooltipState,
): Modifier =
    this.pointerInput(dataList, onPointClick) {
        detectTapGestures { offset ->
            val nearestPoint = findNearestPoint(offset, pointBounds, tapRadius)
            nearestPoint?.let { (position, data) ->
                onPointClick.invoke(data)
                onTooltipStateChange(createTooltipContent(data, position), data)
            } ?: run {
                onTooltipStateChange(null, null)
            }
        }
    }

/**
 * Creates a [TooltipState] anchored to a rectangular bar hit area.
 *
 * @param content Text displayed in the tooltip bubble.
 * @param rect The [Rect] bounds of the tapped bar.
 * @param position Preferred placement of the tooltip.
 */
fun createRectangularTooltipState(
    content: String,
    rect: Rect,
    position: TooltipPosition = TooltipPosition.AUTO,
): TooltipState =
    TooltipState(
        content = content,
        x = rect.left,
        y = rect.top,
        barWidth = rect.width,
        position = position,
    )

/**
 * Creates a [TooltipState] anchored to a point position.
 *
 * @param content Text displayed inside the tooltip bubble.
 * @param position Canvas pixel coordinate of the data point.
 * @param pointRadius Radius of the point marker, used to offset the tooltip anchor.
 * @param tooltipPosition Preferred placement relative to the point.
 * @param pointRadiusMultiplier Multiplier applied to [pointRadius] for arrow positioning.
 */
fun createPointTooltipState(
    content: String,
    position: Offset,
    pointRadius: Float,
    tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    pointRadiusMultiplier: Float = 2f,
): TooltipState =
    TooltipState(
        content = content,
        x = position.x - pointRadius,
        y = position.y,
        barWidth = pointRadius * pointRadiusMultiplier,
        position = tooltipPosition,
    )

/**
 * Tracks a draggable crosshair over a point-based chart.
 *
 * On first touch the crosshair appears immediately (no drag threshold), then follows the
 * finger horizontally, snapping to the nearest data point by x-coordinate. The crosshair
 * is dismissed on finger lift if [dismissOnRelease] is `true`.
 *
 * @param T The type of data associated with each point.
 * @param D The type of items in the data list.
 * @param dataList Used as a recomposition key.
 * @param pointBounds Pixel positions and associated data; updated each draw frame.
 * @param onCrosshairUpdate Called with a new [CrosshairState] and the snapped data item on each
 *   position change, or `(null, null)` when the crosshair should be dismissed.
 * @param labelFormatter Converts a data item to the string shown in the label.
 * @param dismissOnRelease When `true`, the crosshair disappears on finger lift.
 */
fun <T, D> Modifier.chartCrosshairHandler(
    dataList: List<D>,
    pointBounds: List<Pair<Offset, T>>,
    onCrosshairUpdate: (CrosshairState?, T?) -> Unit,
    labelFormatter: (T) -> String,
    dismissOnRelease: Boolean = true,
): Modifier =
    this.pointerInput(dataList) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            down.consume()
            findNearestPointByX(down.position.x, pointBounds)?.let { (position, data) ->
                onCrosshairUpdate(CrosshairState(x = position.x, y = position.y, label = labelFormatter(data)), data)
            }
            var isPressed = true
            while (isPressed) {
                val event = awaitPointerEvent()
                val primary = event.changes.fastFirstOrNull { true } ?: break
                if (primary.pressed) {
                    if (primary.positionChanged()) {
                        primary.consume()
                    }
                    findNearestPointByX(primary.position.x, pointBounds)?.let { (position, data) ->
                        onCrosshairUpdate(
                            CrosshairState(x = position.x, y = position.y, label = labelFormatter(data)),
                            data,
                        )
                    }
                } else {
                    isPressed = false
                }
            }
            if (dismissOnRelease) {
                onCrosshairUpdate(null, null)
            }
        }
    }

/**
 * Tracks a drag-to-scrub tooltip over a chart with rectangular hit areas, such as a bar chart.
 *
 * On first touch the tooltip appears for the item under the finger, then follows the finger as it
 * drags across the chart, jumping to whichever item is under it. The tooltip is dismissed on finger
 * lift when [dismissOnRelease] is `true`. Only horizontal movement is consumed, so a stationary tap
 * still propagates to a co-installed click handler.
 *
 * @param T The data associated with each bound.
 * @param D The type of items in the data list.
 * @param dataList Used as a recomposition key.
 * @param bounds Pairs of [Rect] bounds and associated data; updated each draw frame.
 * @param onTooltipStateChange Callback to push the tracked tooltip (or `(null, null)` to dismiss).
 * @param createTooltipContent Builds a [TooltipState] from the tracked data and its bounds.
 * @param dismissOnRelease When `true`, the tooltip disappears on finger lift.
 */
fun <T, D> Modifier.rectangularChartScrubHandler(
    dataList: List<D>,
    bounds: List<Pair<Rect, T>>,
    onTooltipStateChange: (TooltipState?, T?) -> Unit,
    createTooltipContent: (T, Rect) -> TooltipState,
    dismissOnRelease: Boolean = true,
): Modifier =
    this.pointerInput(dataList) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            findItemByX(down.position, bounds)?.let { (rect, data) ->
                onTooltipStateChange(createTooltipContent(data, rect), data)
            }
            var isPressed = true
            while (isPressed) {
                val event = awaitPointerEvent()
                val primary = event.changes.fastFirstOrNull { true } ?: break
                if (primary.pressed) {
                    if (primary.positionChanged()) {
                        primary.consume()
                    }
                    findItemByX(primary.position, bounds)?.let { (rect, data) ->
                        onTooltipStateChange(createTooltipContent(data, rect), data)
                    }
                } else {
                    isPressed = false
                }
            }
            if (dismissOnRelease) {
                onTooltipStateChange(null, null)
            }
        }
    }

/**
 * Captures a horizontal drag gesture and records it as a brush selection.
 *
 * Writes the drag start and current position into [brushState]. On drag end,
 * [onRangeSelect] is called with the resolved data indices.
 *
 * @param dataList Used as a recomposition key.
 * @param brushState The mutable selection state updated during the gesture.
 * @param onRangeSelect Called with `(startIndex, endIndex)` when the drag ends.
 */
fun <D> Modifier.chartBrushSelectionHandler(
    dataList: List<D>,
    brushState: BrushSelectionState,
    onRangeSelect: ((startIndex: Int, endIndex: Int) -> Unit)?,
): Modifier =
    this.pointerInput(dataList) {
        detectDragGestures(
            onDragStart = { offset -> brushState.start(offset.x) },
            onDrag = { change, _ -> brushState.update(change.position.x) },
            onDragEnd = {
                brushState.toIndexRange()?.let { (start, end) ->
                    onRangeSelect?.invoke(start, end)
                }
                brushState.clear()
            },
            onDragCancel = { brushState.clear() },
        )
    }

/**
 * Enables pinch-to-zoom, drag-to-pan, and inertial fling on a chart.
 *
 * Zoom is centred on the pinch focal point. Drag pans the viewport, and releasing with
 * velocity launches a fling inside [viewPortState] that decelerates smoothly using
 * exponential decay. Starting a new gesture cancels any in-progress fling immediately.
 *
 * The [viewPortState] must have `chartLeft` and `chartWidth` populated by the canvas draw
 * pass, which [com.himanshoe.charty.common.ChartScaffold] does automatically. The fling
 * coroutine scope is injected via [rememberViewPortState][com.himanshoe.charty.common.viewport.rememberViewPortState].
 *
 * @param viewPortState The shared viewport state the chart reads each frame.
 */
fun Modifier.chartZoomAndPan(viewPortState: ViewPortState): Modifier =
    this.pointerInput(viewPortState) {
        awaitEachGesture {
            viewPortState.cancelFling()
            val velocityTracker = VelocityTracker()
            awaitFirstDown(requireUnconsumed = false)
            do {
                val event = awaitPointerEvent()
                val zoom = event.calculateZoom()
                val pan = event.calculatePan()
                val centroid = event.calculateCentroid(useCurrent = true)
                val chartWidth = viewPortState.chartWidth.coerceAtLeast(1f)
                val focusFraction = ((centroid.x - viewPortState.chartLeft) / chartWidth).coerceIn(0f, 1f)
                viewPortState.zoom(focusFraction, zoom)
                viewPortState.pan(-pan.x / chartWidth * viewPortState.visibleFraction)
                event.changes.fastFirstOrNull { true }?.let { change ->
                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                }
                event.changes.fastForEach { it.consume() }
            } while (event.changes.fastAny { it.pressed })
            viewPortState.fling(-velocityTracker.calculateVelocity().x)
        }
    }
