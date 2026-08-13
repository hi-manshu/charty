package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.createPointTooltipState
import com.himanshoe.charty.common.gesture.pointChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint

private const val CROSSHAIR_LABEL_SEPARATOR = "  "

/**
 * The stand-in click callback used when the chart is tapped for a tooltip but the caller passed no
 * click callback of its own. It is a single shared instance so the tap handler's `pointerInput` key
 * stays stable across recompositions.
 */
private val TooltipOnlyTap: (MultilinePoint) -> Unit = {}

/**
 * Resolves what a tap should do: call [onPointClick] when the caller wants clicks, fall back to a
 * shared no-op when only the tooltip needs the gesture, or `null` when no tap handler belongs on the
 * chart at all.
 *
 * @param onPointClick The caller's click callback, or `null` when it ignores clicks.
 * @param tapEnabled Whether anything — a click callback or a tooltip — answers to a tap.
 * @return The callback the tap handler is installed with, or `null` to install none.
 */
internal fun resolveMultilineTapHandler(
    onPointClick: ((MultilinePoint) -> Unit)?,
    tapEnabled: Boolean,
): ((MultilinePoint) -> Unit)? =
    if (tapEnabled) {
        onPointClick ?: TooltipOnlyTap
    } else {
        null
    }

/**
 * Add tap gesture detection for multiline chart points
 */
internal fun Modifier.multilineChartClickHandler(
    dataList: List<LineGroup>,
    lineConfig: LineChartConfig,
    pointBounds: List<Pair<Offset, MultilinePoint>>,
    onPointClick: (MultilinePoint) -> Unit,
    onTooltipStateChange: (TooltipState?, MultilinePoint?) -> Unit,
): Modifier =
    this.pointChartClickHandler(
        dataList = dataList,
        pointBounds = pointBounds,
        tapRadius = lineConfig.pointRadius * MultilineChartConstants.TAP_RADIUS_MULTIPLIER,
        onPointClick = onPointClick,
        onTooltipStateChange = onTooltipStateChange,
        createTooltipContent = { point, position ->
            createPointTooltipState(
                content = lineConfig.tooltipFormatter(point.toTooltipLineData()),
                position = position,
                pointRadius = lineConfig.pointRadius,
                tooltipPosition = lineConfig.tooltipPosition,
                pointRadiusMultiplier = MultilineChartConstants.POINT_RADIUS_MULTIPLIER,
            )
        },
    )

/**
 * Chains the multiline chart's pointer handling. Tap-to-tooltip and the crosshair drag are
 * installed independently — a tap never travels past touch slop and a crosshair only engages once
 * it does — so a chart configured with both answers to both. Brush selection and zoom/pan are
 * layered on top by [buildInteractionModifier].
 *
 * @param base The modifier the handlers are chained onto.
 * @param crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @param dataList The groups currently drawn, which the handlers hit-test against.
 * @param lineConfig Supplies the crosshair's dismiss behaviour and the tap radius.
 * @param pointBounds The per-point bounds the tap handler tests.
 * @param crosshairBounds The first-series bounds the crosshair snaps to.
 * @param onPointTap Invoked when a point is tapped, or `null` to install no tap handler; see
 *   [resolveMultilineTapHandler].
 * @param onTooltipStateChange Receives the tooltip raised by a tap, and the point it belongs to.
 * @param interactionConfig Supplies the brush-selection and viewport state holders.
 * @return The chained [Modifier] to apply to the chart.
 */
internal fun buildMultilineModifier(
    base: Modifier,
    crosshairManager: CrosshairManager<MultilinePoint>?,
    dataList: List<LineGroup>,
    lineConfig: LineChartConfig,
    pointBounds: MutableList<Pair<Offset, MultilinePoint>>,
    crosshairBounds: MutableList<Pair<Offset, MultilinePoint>>,
    onPointTap: ((MultilinePoint) -> Unit)?,
    onTooltipStateChange: (TooltipState?, MultilinePoint?) -> Unit,
    interactionConfig: ChartInteractionConfig,
): Modifier {
    var mod: Modifier = Modifier
    if (onPointTap != null) {
        mod =
            mod.multilineChartClickHandler(
                dataList = dataList,
                lineConfig = lineConfig,
                pointBounds = pointBounds,
                onPointClick = onPointTap,
                onTooltipStateChange = onTooltipStateChange,
            )
    }
    if (crosshairManager != null) {
        mod =
            mod.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = crosshairBounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = { point ->
                    point.lineGroup.values
                        .mapIndexed { seriesIndex, value ->
                            lineConfig.tooltipFormatter(
                                crosshairSeriesLineData(seriesIndex = seriesIndex, value = value),
                            )
                        }.joinToString(separator = CROSSHAIR_LABEL_SEPARATOR)
                },
                dismissOnRelease = lineConfig.crosshairConfig?.dismissOnRelease ?: true,
            )
    }
    return buildInteractionModifier(base = base.then(mod), interactionConfig = interactionConfig, dataList = dataList)
}
