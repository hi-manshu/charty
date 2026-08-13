package com.himanshoe.charty.gantt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingPan
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.gantt.config.GanttChartConfig
import com.himanshoe.charty.gantt.data.GanttRow
import com.himanshoe.charty.gantt.data.GanttSelection
import com.himanshoe.charty.gantt.internal.GanttDrawParams
import com.himanshoe.charty.gantt.internal.createGanttChartModifier
import com.himanshoe.charty.gantt.internal.drawGanttRows
import com.himanshoe.charty.gantt.internal.ganttAccessibility
import com.himanshoe.charty.gantt.internal.ganttAxisConfig
import com.himanshoe.charty.gantt.internal.ganttValueRange
import com.himanshoe.charty.gantt.internal.generateGanttChartDescription
import com.himanshoe.charty.gantt.internal.rememberGanttSegmentLabels

/**
 * A Gantt chart: one row per category, each carrying **many** segments laid out along a shared
 * continuous axis. Schedules, timelines, shift rosters, machine availability — anything where a row
 * is busy in several separate stretches.
 *
 * This is what [com.himanshoe.charty.bar.SpanChart] cannot express: a span chart draws exactly one
 * range per row, so a task that pauses and resumes needs a row per stretch. Here the row keeps its
 * identity and holds a list of [com.himanshoe.charty.gantt.data.GanttSegment]s, which may not
 * overlap each other (see [GanttRow]).
 *
 * **The axis is generic, not a clock.** Values are plain floats — hours into a shift, kilometres
 * along a route, or instants. To plot real time, pass instants as values and format the ticks
 * yourself through [GanttChartConfig.valueFormatter];
 * [com.himanshoe.charty.common.axis.selectDateTimeTicks] and
 * [com.himanshoe.charty.common.axis.dateTimeAxisLabels] give the same minute/hour/day/month snapping
 * the datetime axis uses, so a formatter can round a value to the nearest tick and reuse its label.
 * Note that a `Float` carries about seven significant digits,
 * which is not enough to keep epoch-millisecond values distinct: measure from a reference instant
 * (millis since the start of the schedule) or pass seconds rather than raw epoch millis.
 *
 * Example:
 * ```kotlin
 * GanttChart(
 *     data = {
 *         listOf(
 *             GanttRow(
 *                 label = "Design",
 *                 segments = listOf(
 *                     GanttSegment(startValue = 0f, endValue = 3f, label = "Draft"),
 *                     GanttSegment(startValue = 5f, endValue = 8f, progress = 0.5f),
 *                 ),
 *             ),
 *             GanttRow(label = "Build", segments = listOf(GanttSegment(startValue = 3f, endValue = 9f))),
 *         )
 *     },
 * )
 * ```
 *
 * @param data Lambda returning the rows to display.
 * @param modifier The modifier applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when `null`
 *   (default) a built-in "No data" state is used.
 * @param color The default segment colour, unless a segment overrides it.
 * @param ganttConfig Appearance and behaviour: bar thickness, corner radius, animation, progress
 *   dimming, segment labels, and value formatting.
 * @param scaffoldConfig Styling for the axis, grid, and labels.
 * @param onSegmentClick Called when a segment is tapped, with the segment and its row.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Composable
fun GanttChart(
    data: () -> List<GanttRow>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    ganttConfig: GanttChartConfig = GanttChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onSegmentClick: ((GanttSelection) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<GanttSelection> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = ganttConfig.animation,
            visibleWindow = ganttConfig.visibleWindow,
            describe = { series, min, max ->
                generateGanttChartDescription(
                    rows = series,
                    minValue = min,
                    maxValue = max,
                    valueFormatter = ganttConfig.valueFormatter,
                )
            },
        ) { windowed, _ -> rememberGanttValueRange(rows = windowed) }
    val rows = chartState.data
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue
    val textMeasurer = rememberTextMeasurer()
    val labels = rememberGanttSegmentLabels(rows = rows, config = ganttConfig, textMeasurer = textMeasurer)
    val tooltipManager = rememberTooltipManager<Rect, GanttSelection>(dataKey = rows)

    val chartModifier =
        buildInteractionModifier(
            base =
                createGanttChartModifier(
                    modifier = modifier,
                    onSegmentClick = onSegmentClick,
                    rows = rows,
                    config = ganttConfig,
                    segmentBounds = tooltipManager.bounds,
                    onTooltipUpdate = tooltipManager::updateTooltip,
                    enableScrub = interactionConfig.dragTooltipActive,
                ),
            interactionConfig = interactionConfig,
            dataList = rows,
        )
    val pan =
        interactionConfig.streamingPan(
            streaming = chartState.streaming,
            orientation = ChartOrientation.HORIZONTAL,
        )

    Box(modifier = chartModifier.then(pan)) {
        ChartScaffold(
            accessibility =
                ganttAccessibility(
                    description = chartState.description,
                    rows = rows,
                    valueFormatter = ganttConfig.valueFormatter,
                ),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = rows.fastMap { row -> row.label },
            yAxisConfig =
                ganttAxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    steps = ganttConfig.axisSteps,
                    valueFormatter = ganttConfig.valueFormatter,
                ),
            config = scaffoldConfig,
            orientation = ChartOrientation.HORIZONTAL,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)
            tooltipManager.clearBounds()

            drawGanttRows(
                GanttDrawParams(
                    rows = rows,
                    chartContext = chartContext,
                    config = ganttConfig,
                    color = color,
                    minValue = minValue,
                    maxValue = maxValue,
                    animationProgress = chartState.animationProgress.value,
                    labels = labels,
                    onSegmentBoundCalculated = { bounds -> tooltipManager.bounds.add(bounds) },
                    recordBounds = onSegmentClick != null || interactionConfig.dragTooltipActive,
                ),
            )

            if (tooltip.isCanvas()) {
                drawTooltipIfNeeded(
                    tooltipState = tooltipManager.tooltipState,
                    tooltipConfig = ganttConfig.tooltipConfig,
                    textMeasurer = textMeasurer,
                    chartContext = chartContext,
                )
            }

            drawInteractionOverlays(
                interactionConfig = interactionConfig,
                chartContext = chartContext,
                totalItems = rows.size,
                textMeasurer = textMeasurer,
            )
        }

        ChartTooltipHost(
            tooltip = tooltip,
            item = tooltipManager.selectedItem,
            anchor = tooltipManager.tooltipState,
            modifier = Modifier.matchParentSize(),
        )
    }
}

@Composable
private fun rememberGanttValueRange(rows: List<GanttRow>): Pair<Float, Float> =
    remember(rows) { ganttValueRange(rows = rows) }
