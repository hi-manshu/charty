package com.himanshoe.charty.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.common.annotation.drawChartAnnotation
import com.himanshoe.charty.common.brush.BrushSelectionState
import com.himanshoe.charty.common.brush.drawBrushSelection
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.chartBrushSelectionHandler
import com.himanshoe.charty.common.gesture.chartZoomAndPan
import com.himanshoe.charty.common.viewport.ViewPortState

/**
 * Computes the windowed/visible slice of [fullDataList] for the current viewport.
 * When [viewPortState] is null, returns [fullDataList] as-is.
 */
@Composable
internal fun <T> rememberWindowedData(
    fullDataList: List<T>,
    viewPortState: ViewPortState?,
): List<T> =
    remember(fullDataList, viewPortState?.startFraction, viewPortState?.endFraction) {
        if (viewPortState == null) {
            fullDataList
        } else {
            val range = viewPortState.visibleIndices(fullDataList.size)
            fullDataList.subList(range.first, range.last + 1)
        }
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
}
