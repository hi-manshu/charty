package com.himanshoe.charty.point

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.calculateDistance
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.common.util.toChartLabel
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.BubbleData
import com.himanshoe.charty.point.data.PointData
import kotlin.math.sqrt

private const val BUBBLE_DIAMETER_MULTIPLIER = 2f

/**
 * Default normalized size when size range is zero
 */
internal const val DEFAULT_NORMALIZED_SIZE = 0.5f

/**
 * A data class that holds information about the size of bubbles in a bubble chart.
 *
 * @property minValue The minimum y-value in the dataset.
 * @property maxValue The maximum y-value in the dataset.
 * @property minSize The minimum size value in the dataset.
 * @property sizeRange How far the largest bubble's size is above the smallest.
 */
internal data class BubbleSizeInfo(
    val minValue: Float,
    val maxValue: Float,
    val minSize: Float,
    val sizeRange: Float,
)

/**
 * A data class that holds the drawn circle of a bubble, which is its hit area.
 *
 * @property center The center coordinates of the bubble.
 * @property radius The radius of the bubble.
 */
internal data class BubbleBounds(
    val center: Offset,
    val radius: Float,
)

/**
 * Resolves the bubble whose drawn circle contains [position], or `null` when the tap landed between
 * bubbles or outside the plot. Bubbles vary in radius, so the hit area is the circle itself rather
 * than a uniform column: a tap one pixel outside a small bubble misses it even though it is the
 * nearest one. Overlapping bubbles are resolved front to back, matching the order they were drawn.
 *
 * @param bubbleBounds The drawn circles paired with their data, refilled each draw pass.
 * @param position The tap position, in canvas pixels.
 * @return The hit bubble with its circle, or `null` when nothing was hit.
 */
internal fun resolveBubbleAt(
    bubbleBounds: List<Pair<BubbleBounds, BubbleData>>,
    position: Offset,
): Pair<BubbleBounds, BubbleData>? =
    bubbleBounds.fastFirstOrNull { (bounds, _) ->
        calculateDistance(bounds.center, position) <= bounds.radius
    }

/**
 * Builds the tooltip anchor for a tapped bubble, anchored to the top of its circle so the bubble is
 * never covered by its own tooltip.
 *
 * [TooltipState.x] is the anchor's leading edge, not its centre: the drawer centres the bubble over
 * `x + barWidth / 2`, which for a circle's left edge and diameter lands on the bubble's centre.
 *
 * @param bounds The tapped bubble's drawn circle.
 * @param content The formatted tooltip text.
 * @param position The preferred placement relative to the bubble.
 * @return The anchor the tooltip is drawn against.
 */
internal fun bubbleTooltipState(
    bounds: BubbleBounds,
    content: String,
    position: TooltipPosition = TooltipPosition.AUTO,
): TooltipState =
    TooltipState(
        content = content,
        x = bounds.center.x - bounds.radius,
        y = bounds.center.y - bounds.radius,
        barWidth = bounds.radius * BUBBLE_DIAMETER_MULTIPLIER,
        position = position,
    )

/**
 * Projects a bubble onto the [PointData] shape that
 * [com.himanshoe.charty.point.config.PointChartConfig.tooltipFormatter] accepts: the bubble's label
 * qualified with the size it encodes, paired with its y value. The size is folded into the label
 * because [PointData] carries only a label and one value, and dropping it would hide the very
 * dimension that makes a bubble a bubble.
 *
 * @receiver The tapped bubble.
 * @return The formatter's input for this bubble.
 */
internal fun BubbleData.toTooltipPointData(): PointData =
    PointData(label = "$label (size ${size.toChartLabel()})", value = yValue)

/**
 * Calculates the [BubbleSizeInfo] from a list of [BubbleData].
 *
 * @param dataList The list of [BubbleData].
 * @return A [BubbleSizeInfo] instance containing the calculated size information.
 */
internal fun calculateBubbleSizeInfo(dataList: List<BubbleData>): BubbleSizeInfo {
    val yValues = dataList.fastMap { it.yValue }
    val sizes = dataList.fastMap { it.size }
    val smallest = sizes.minOrNull() ?: 0f
    val largest = sizes.maxOrNull() ?: 1f
    return BubbleSizeInfo(
        minValue = calculateMinValue(yValues),
        maxValue = calculateMaxValue(yValues),
        minSize = smallest,
        sizeRange = largest - smallest,
    )
}

/**
 * Calculates the radius for a bubble based on its size.
 *
 * @param bubbleSize The size of the bubble.
 * @param minSize The minimum size in the dataset.
 * @param sizeRange The range of sizes in the dataset.
 * @param minBubbleRadius The minimum allowed radius for a bubble.
 * @param maxBubbleRadius The maximum allowed radius for a bubble.
 * @return The calculated radius for the bubble.
 */
internal fun calculateBubbleRadius(
    bubbleSize: Float,
    minSize: Float,
    sizeRange: Float,
    minBubbleRadius: Float,
    maxBubbleRadius: Float,
): Float {
    val normalizedSize =
        if (sizeRange > 0f) {
            (bubbleSize - minSize) / sizeRange
        } else {
            DEFAULT_NORMALIZED_SIZE
        }
    val radiusRange = maxBubbleRadius - minBubbleRadius
    return minBubbleRadius + (sqrt(normalizedSize) * radiusRange)
}

/**
 * Creates the bubble chart's tap modifier: one gesture that both invokes [onBubbleClick] and raises
 * the tooltip for the bubble under the finger, so neither displaces the other. A tap that hits no
 * bubble dismisses the tooltip.
 *
 * @param dataList The list of [BubbleData], used as a recomposition key.
 * @param bubbleBounds The drawn circles paired with their data, for hit detection.
 * @param onBubbleClick A lambda function to be invoked when a bubble is clicked.
 * @param config Supplies the tooltip text and its preferred placement.
 * @param onTooltipUpdate Receives the tooltip raised by a tap, and the bubble it belongs to.
 * @param tapEnabled When `false`, no tap handler is installed at all.
 * @return A [Modifier] that handles tap gestures for the bubble chart.
 */
internal fun createBubbleClickModifier(
    dataList: List<BubbleData>,
    bubbleBounds: List<Pair<BubbleBounds, BubbleData>>,
    onBubbleClick: ((BubbleData) -> Unit)?,
    config: PointChartConfig,
    onTooltipUpdate: (TooltipState?, BubbleData?) -> Unit,
    tapEnabled: Boolean,
): Modifier =
    if (tapEnabled) {
        Modifier.pointerInput(dataList, onBubbleClick) {
            detectTapGestures { tapOffset ->
                val hit = resolveBubbleAt(bubbleBounds = bubbleBounds, position = tapOffset)
                if (hit == null) {
                    onTooltipUpdate(null, null)
                } else {
                    onBubbleClick?.invoke(hit.second)
                    onTooltipUpdate(
                        bubbleTooltipState(
                            bounds = hit.first,
                            content = config.tooltipFormatter(hit.second.toTooltipPointData()),
                            position = config.tooltipPosition,
                        ),
                        hit.second,
                    )
                }
            }
        }
    } else {
        Modifier
    }

/**
 * Chains the bubble chart's pointer handlers. Tap-to-click and the crosshair drag are installed
 * independently — a tap never travels past touch slop and a crosshair only engages once it does —
 * so a chart configured with both answers to both.
 *
 * @param dataList The bubbles currently drawn, used as a recomposition key.
 * @param bubbleBounds The drawn bubble circles the tap handler hit-tests.
 * @param crosshairBounds The per-bubble anchors the crosshair snaps to.
 * @param onBubbleClick Invoked when a bubble is tapped, or `null` when the caller ignores clicks.
 * @param crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @param dismissOnRelease When `true`, the crosshair disappears on finger lift.
 * @param config Supplies the tooltip text and its preferred placement.
 * @param onTooltipUpdate Receives the tooltip raised by a tap, and the bubble it belongs to.
 * @param tapEnabled When `false`, no tap handler is installed at all.
 * @return The chained [Modifier] to apply to the chart.
 */
@Suppress("LongParameterList") // Every handler this chains needs its own state holder.
internal fun buildBubbleGestureModifier(
    dataList: List<BubbleData>,
    bubbleBounds: List<Pair<BubbleBounds, BubbleData>>,
    crosshairBounds: List<Pair<Offset, BubbleData>>,
    onBubbleClick: ((BubbleData) -> Unit)?,
    crosshairManager: CrosshairManager<BubbleData>?,
    dismissOnRelease: Boolean,
    config: PointChartConfig,
    onTooltipUpdate: (TooltipState?, BubbleData?) -> Unit,
    tapEnabled: Boolean,
): Modifier {
    val clickModifier =
        createBubbleClickModifier(
            dataList = dataList,
            bubbleBounds = bubbleBounds,
            onBubbleClick = onBubbleClick,
            config = config,
            onTooltipUpdate = onTooltipUpdate,
            tapEnabled = tapEnabled,
        )
    return if (crosshairManager != null) {
        clickModifier.chartCrosshairHandler(
            dataList = dataList,
            pointBounds = crosshairBounds,
            onCrosshairUpdate = crosshairManager::update,
            labelFormatter = { bubble -> config.tooltipFormatter(bubble.toTooltipPointData()) },
            dismissOnRelease = dismissOnRelease,
        )
    } else {
        clickModifier
    }
}
