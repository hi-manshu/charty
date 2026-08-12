package com.himanshoe.charty.common

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.himanshoe.charty.common.animation.rememberAnimatedRange
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig

private val EmptyValueRange = 0f to 0f

/**
 * The state every Cartesian chart resolves before it draws: which slice of the series is visible,
 * the axis range that slice maps onto, how far the reveal animation has progressed, and what a
 * screen reader should say about it. Produced by [rememberCartesianChartState].
 *
 * [data] and [displayData] are deliberately separate. [data] is the windowed truth and is what
 * hit-testing, axis labels, and data-size bookkeeping must use; [displayData] is the same series
 * with its values tweened toward their targets, and is what the canvas draws. When a chart does not
 * animate value changes the two are the same list.
 *
 * @property fullData The complete series the caller supplied, before any windowing.
 * @property data The windowed — and where enabled, downsampled — points the chart works with.
 * @property displayData [data] with per-value change animation applied, ready to draw.
 * @property streaming The layout sliding a rolling window, or `null` when the chart draws statically.
 * @property minValue The axis minimum for this frame, already smoothed while streaming.
 * @property maxValue The axis maximum for this frame, already smoothed while streaming.
 * @property animationProgress The reveal progress from `0f` to `1f`. Kept as an [Animatable] rather
 *   than a plain `Float` so drawing code reads `.value` inside the draw phase and each animation
 *   frame redraws without recomposing the chart.
 * @property description The chart-level accessibility summary, or `null` when the chart supplies no
 *   summary generator or the caller suppressed the description.
 */
@Immutable
internal class CartesianChartState<T>(
    val fullData: List<T>,
    val data: List<T>,
    val displayData: List<T>,
    val streaming: StreamingLayout?,
    val minValue: Float,
    val maxValue: Float,
    val animationProgress: Animatable<Float, *>,
    val description: String?,
)

/**
 * Resolves the opening sequence shared by every Cartesian chart, in the one order they all need it:
 * window the series (see [rememberVisibleData]), tween its values, derive the axis range from the
 * result, smooth that range while streaming (see [rememberAnimatedRange]), start the reveal
 * animation, build the accessibility summary, and publish the resulting data sizes to the
 * interaction state holders.
 *
 * Every hook inside runs on every composition — the optional behaviour is expressed through the
 * default lambdas rather than through skipped calls — so a chart never changes its hook order by
 * switching between static, windowed, and streaming modes.
 *
 * Charts differ in where their range comes from: some read the windowed values, some the tweened
 * ones, some pin the minimum to zero and animate only the maximum. [range] receives both lists and
 * returns the pair it wants, which expresses all three without a further parameter. A chart with no
 * value axis at all — a normalized or mosaic layout, where every bar fills its slot — simply omits
 * [range] and ignores [CartesianChartState.minValue] and [CartesianChartState.maxValue].
 *
 * @param fullData The complete series. Callers must handle the empty case before calling, since a
 *   composable cannot return early on their behalf.
 * @param interactionConfig Supplies the viewport, streaming, brush-selection, and accessibility
 *   settings, and receives the resolved data sizes.
 * @param animation Drives the reveal, the value tweening, and the streaming range smoothing.
 * @param visibleWindow The rolling "show last N" window, or `null` to show everything.
 * @param downsampleThreshold The maximum points to render, or `null` to disable downsampling.
 * @param downsampleValue Extracts the y-value that downsampling preserves the shape of. Required
 *   only when [downsampleThreshold] is set.
 * @param displayData Applies per-value change animation to the windowed points; defaults to passing
 *   them through untouched.
 * @param describe Builds the accessibility summary from the full series and the resolved axis
 *   range, or `null` when the chart provides its own accessibility payload.
 * @param range Derives the raw `(min, max)` axis range from the windowed points and the tweened
 *   points; defaults to a zero range for charts without a value axis.
 * @return The resolved state for this frame.
 */
@Composable
internal fun <T> rememberCartesianChartState(
    fullData: List<T>,
    interactionConfig: ChartInteractionConfig,
    animation: Animation,
    visibleWindow: Int?,
    downsampleThreshold: Int? = null,
    downsampleValue: ((T) -> Float)? = null,
    displayData: @Composable (List<T>) -> List<T> = { it },
    describe: ((data: List<T>, minValue: Float, maxValue: Float) -> String)? = null,
    range: @Composable (data: List<T>, displayData: List<T>) -> Pair<Float, Float> = { _, _ -> EmptyValueRange },
): CartesianChartState<T> {
    val visible =
        rememberVisibleData(
            fullDataList = fullData,
            interactionConfig = interactionConfig,
            downsampleThreshold = downsampleThreshold,
            visibleWindow = visibleWindow,
            animation = animation,
            value = { item -> downsampleValue?.invoke(item) ?: 0f },
        )
    val data = visible.data
    val display = displayData(data)
    val (rawMinValue, rawMaxValue) = range(data, display)
    val (minValue, maxValue) =
        rememberAnimatedRange(
            minValue = rawMinValue,
            maxValue = rawMaxValue,
            animation = animation,
            active = visible.streaming != null,
        )
    val animationProgress = rememberChartAnimation(animation)
    val generatedDescription =
        rememberChartDescription(fullData, interactionConfig.accessibilityDescription) { series ->
            describe?.invoke(series, minValue, maxValue).orEmpty()
        }
    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullData.size,
        dataSize = data.size,
    )
    return CartesianChartState(
        fullData = fullData,
        data = data,
        displayData = display,
        streaming = visible.streaming,
        minValue = minValue,
        maxValue = maxValue,
        animationProgress = animationProgress,
        description = if (describe == null) null else generatedDescription,
    )
}
