@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
    "ktlint:standard:max-line-length",
    "ktlint:standard:function-naming",
)

package com.himanshoe.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.HorizontalBarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.streaming.ChartJumpToLatestPill
import com.himanshoe.charty.common.streaming.StreamingState
import com.himanshoe.charty.common.streaming.rememberStreamingState
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.line.AreaChart
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.point.PointChart
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.PointData
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val AUTO_INTERVAL_MILLIS = 700L
private const val SEED_COUNT = 12
private const val VALUE_MIN = 15f
private const val VALUE_MAX = 95f

private fun nextValue(): Float = VALUE_MIN + Random.nextFloat() * (VALUE_MAX - VALUE_MIN)

internal enum class StreamChartType(
    val label: String,
) {
    Line("Line"),
    Area("Area"),
    Bar("Bar"),
    HorizontalBar("Horizontal bar"),
    Point("Point"),
}

internal enum class StreamTooltipMode(
    val label: String,
) {
    Canvas("Canvas"),
    Compose("Compose"),
    None("None"),
}

internal enum class StreamJumpOverlay(
    val label: String,
) {
    None("None"),
    Pill("Built-in pill"),
    Custom("Custom"),
}

/**
 * Everything the per-chart-type renderers need, so each one keeps a two-argument signature instead of
 * threading a dozen flags through.
 */
@Stable
private class StreamOptions(
    val windowSize: Int,
    val color: ChartyColor,
    val animation: Animation,
    val markers: List<PersistentMarker>,
    val interactionConfig: ChartInteractionConfig,
    val tooltipMode: StreamTooltipMode,
    val crosshairEnabled: Boolean,
) {
    val tapEnabled: Boolean get() = tooltipMode != StreamTooltipMode.None
}

/**
 * Live streaming demo driving the real charts with a rolling `visibleWindow`. The chart owns the
 * slide: appending to the data list advances the window and the production animation eases the last
 * `windowSize` points — and their x-axis labels — leftwards while the new point enters at the right.
 *
 * On top of the window it demonstrates the rest of the streaming surface: [rememberStreamingState]
 * scrollback, a built-in or fully custom "jump to latest" overlay, the three tooltip strategies, and
 * a draggable crosshair. There is no hand-rolled animation here; this is exactly the public API a
 * consumer would use.
 */
@Composable
internal fun StreamingLinePlayground() {
    var windowSize by remember { mutableStateOf(8) }
    var auto by remember { mutableStateOf(true) }
    var color by remember { mutableStateOf(playgroundPalette[0]) }
    var chartType by remember { mutableStateOf(StreamChartType.Line) }
    var labelNewest by remember { mutableStateOf(true) }
    var scrollback by remember { mutableStateOf(true) }
    var jumpOverlay by remember { mutableStateOf(StreamJumpOverlay.Pill) }
    var tooltipMode by remember { mutableStateOf(StreamTooltipMode.Canvas) }
    var crosshairEnabled by remember { mutableStateOf(false) }

    val streamingState = rememberStreamingState()
    val values = remember { mutableStateListOf<Float>().apply { repeat(SEED_COUNT) { add(nextValue()) } } }

    fun append() {
        values.add(nextValue())
    }

    LaunchedEffect(auto) {
        while (auto) {
            delay(AUTO_INTERVAL_MILLIS)
            append()
        }
    }

    val animation =
        if (LocalPlaygroundAnimate.current) {
            Animation.Fast
        } else {
            Animation.Disabled
        }
    val markers =
        if (labelNewest) {
            listOf(PersistentMarker(dataIndex = -1))
        } else {
            emptyList()
        }
    val supportsCrosshair = chartType != StreamChartType.Bar && chartType != StreamChartType.HorizontalBar
    val options =
        StreamOptions(
            windowSize = windowSize,
            color = ChartyColor.Solid(color),
            animation = animation,
            markers = markers,
            interactionConfig =
                ChartInteractionConfig(
                    streamingState =
                        if (scrollback) {
                            streamingState
                        } else {
                            null
                        },
                    jumpToLatest = jumpToLatestSlot(overlay = jumpOverlay, enabled = scrollback),
                ),
            tooltipMode = tooltipMode,
            crosshairEnabled = crosshairEnabled && supportsCrosshair,
        )

    val code =
        streamingCode(
            chartType = chartType,
            windowSize = windowSize,
            scrollback = scrollback,
            jumpOverlay = jumpOverlay,
            tooltipMode = tooltipMode,
            crosshairEnabled = crosshairEnabled && supportsCrosshair,
            labelNewest = labelNewest,
            animate = LocalPlaygroundAnimate.current,
        )

    PlaygroundScaffold(
        code = code,
        chart = { StreamingChart(chartType = chartType, values = values, options = options) },
        controls = {
            ControlSection(title = "Chart")
            ChoiceRow(
                label = "Type",
                options = StreamChartType.entries.toList(),
                selected = chartType,
                labelOf = { it.label },
                onSelect = { chartType = it },
            )
            ControlSection(title = "Stream")
            SwitchRow(label = "Auto (add every 0.7s)", checked = auto, onCheckedChange = { auto = it })
            SwitchRow(label = "Label newest point", checked = labelNewest, onCheckedChange = { labelNewest = it })
            PlaygroundActionRow(
                primaryLabel = "Add point",
                onPrimary = { append() },
                secondaryLabel = "Clear",
                onSecondary = {
                    values.clear()
                    repeat(SEED_COUNT) { values.add(nextValue()) }
                },
            )
            ControlSection(title = "Window")
            IntSliderRow(
                label = "Visible points",
                value = windowSize,
                valueRange = 4..20,
                onValueChange = { windowSize = it },
            )
            ControlSection(title = "Scrollback")
            SwitchRow(label = "Drag back through history", checked = scrollback, onCheckedChange = { scrollback = it })
            ChoiceRow(
                label = "Jump-to-latest overlay",
                options = StreamJumpOverlay.entries.toList(),
                selected = jumpOverlay,
                labelOf = { it.label },
                onSelect = { jumpOverlay = it },
            )
            Text(
                text = "Drag the plot leftwards to detach the window; the overlay appears with the pending count and jumps back on tap.",
                style = MaterialTheme.typography.bodySmall,
            )
            ControlSection(title = "Tooltip")
            ChoiceRow(
                label = "Strategy",
                options = StreamTooltipMode.entries.toList(),
                selected = tooltipMode,
                labelOf = { it.label },
                onSelect = { tooltipMode = it },
            )
            ControlSection(title = "Crosshair")
            SwitchRow(
                label = "Draggable crosshair",
                checked = crosshairEnabled,
                onCheckedChange = { crosshairEnabled = it },
            )
            Text(
                text =
                    if (supportsCrosshair) {
                        "The crosshair owns the drag gesture, so it takes over from both the tap tooltip and scrollback while it is on."
                    } else {
                        "Bar charts have no crosshair parameter — switch to Line, Area or Point."
                    },
                style = MaterialTheme.typography.bodySmall,
            )
            ControlSection(title = "Live")
            Text(
                text = "total points: ${values.size}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text =
                    if (streamingState.isFollowing) {
                        "window: following the newest point"
                    } else {
                        "window: detached · ${streamingState.pendingCount} pending"
                    },
                style = MaterialTheme.typography.bodySmall,
            )
            ControlSection(title = "Color")
            ColorRow(label = "Line color", selected = color, onSelect = { color = it })
        },
    )
}

private fun jumpToLatestSlot(
    overlay: StreamJumpOverlay,
    enabled: Boolean,
): (@Composable (StreamingState) -> Unit)? {
    val pill: @Composable (StreamingState) -> Unit = { state -> ChartJumpToLatestPill(state = state) }
    val custom: @Composable (StreamingState) -> Unit = { state -> CustomJumpToLatest(state = state) }
    if (!enabled) {
        return null
    }
    return when (overlay) {
        StreamJumpOverlay.None -> null
        StreamJumpOverlay.Pill -> pill
        StreamJumpOverlay.Custom -> custom
    }
}

private fun <T> streamClick(enabled: Boolean): ((T) -> Unit)? {
    val noop: (T) -> Unit = { }
    return if (enabled) {
        noop
    } else {
        null
    }
}

/**
 * A hand-rolled replacement for [ChartJumpToLatestPill], proving the `jumpToLatest` slot takes any
 * composable: the chart only decides when it is visible and hands over the [StreamingState].
 */
@Composable
private fun CustomJumpToLatest(state: StreamingState) {
    val scope = rememberCoroutineScope()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF212121),
        shadowElevation = 6.dp,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { scope.launch { state.jumpToLatest() } },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE53935)))
            Text(
                text = "LIVE",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "+${state.pendingCount} waiting",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(text = "▶", color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun StreamingBubble(text: String) {
    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF1E88E5), shadowElevation = 4.dp) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

private fun <T> streamTooltip(mode: StreamTooltipMode): ChartTooltip<T> =
    when (mode) {
        StreamTooltipMode.Canvas -> ChartTooltip.canvas()
        StreamTooltipMode.Compose -> ChartTooltip.compose { StreamingBubble(text = text) }
        StreamTooltipMode.None -> ChartTooltip.none()
    }

private fun <T> streamCrosshair(enabled: Boolean): ChartCrosshair<T>? =
    if (enabled) {
        ChartCrosshair(config = ChartCrosshairConfig(showHorizontalLine = false, dismissOnRelease = true))
    } else {
        null
    }

@Composable
private fun StreamingChart(
    chartType: StreamChartType,
    values: List<Float>,
    options: StreamOptions,
) {
    when (chartType) {
        StreamChartType.Line -> StreamingLine(values = values, options = options)
        StreamChartType.Area -> StreamingArea(values = values, options = options)
        StreamChartType.Bar -> StreamingBar(values = values, options = options)
        StreamChartType.HorizontalBar -> StreamingHorizontalBar(values = values, options = options)
        StreamChartType.Point -> StreamingPoint(values = values, options = options)
    }
}

@Composable
private fun StreamingLine(
    values: List<Float>,
    options: StreamOptions,
) {
    val data = values.mapIndexed { index, value -> LineData(label = (index + 1).toString(), value = value) }
    LineChart(
        data = { data },
        modifier = Modifier.fillMaxSize(),
        color = options.color,
        lineConfig =
            LineChartConfig(
                visibleWindow = options.windowSize,
                animation = options.animation,
                markers = options.markers,
            ),
        onPointClick = streamClick(enabled = options.tapEnabled),
        interactionConfig = options.interactionConfig,
        crosshair = streamCrosshair(enabled = options.crosshairEnabled),
        tooltip = streamTooltip(mode = options.tooltipMode),
    )
}

@Composable
private fun StreamingArea(
    values: List<Float>,
    options: StreamOptions,
) {
    val data = values.mapIndexed { index, value -> LineData(label = (index + 1).toString(), value = value) }
    AreaChart(
        data = { data },
        modifier = Modifier.fillMaxSize(),
        color = options.color,
        lineConfig =
            LineChartConfig(
                visibleWindow = options.windowSize,
                animation = options.animation,
                markers = options.markers,
            ),
        onPointClick = streamClick(enabled = options.tapEnabled),
        interactionConfig = options.interactionConfig,
        tooltip = streamTooltip(mode = options.tooltipMode),
        crosshair = streamCrosshair(enabled = options.crosshairEnabled),
    )
}

@Composable
private fun StreamingBar(
    values: List<Float>,
    options: StreamOptions,
) {
    val data = values.mapIndexed { index, value -> BarData(label = (index + 1).toString(), value = value) }
    BarChart(
        data = { data },
        modifier = Modifier.fillMaxSize(),
        color = options.color,
        barConfig =
            BarChartConfig(
                visibleWindow = options.windowSize,
                animation = options.animation,
                markers = options.markers,
            ),
        onBarClick = streamClick(enabled = options.tapEnabled),
        interactionConfig = options.interactionConfig,
        tooltip = streamTooltip(mode = options.tooltipMode),
    )
}

@Composable
private fun StreamingHorizontalBar(
    values: List<Float>,
    options: StreamOptions,
) {
    val data = values.mapIndexed { index, value -> BarData(label = (index + 1).toString(), value = value) }
    HorizontalBarChart(
        data = { data },
        modifier = Modifier.fillMaxSize(),
        color = options.color,
        barConfig =
            BarChartConfig(
                visibleWindow = options.windowSize,
                animation = options.animation,
                markers = options.markers,
            ),
        onBarClick = streamClick(enabled = options.tapEnabled),
        interactionConfig = options.interactionConfig,
        tooltip = streamTooltip(mode = options.tooltipMode),
    )
}

@Composable
private fun StreamingPoint(
    values: List<Float>,
    options: StreamOptions,
) {
    val data = values.mapIndexed { index, value -> PointData(label = (index + 1).toString(), value = value) }
    PointChart(
        data = { data },
        modifier = Modifier.fillMaxSize(),
        color = options.color,
        pointConfig =
            PointChartConfig(
                visibleWindow = options.windowSize,
                animation = options.animation,
                markers = options.markers,
            ),
        onPointClick = streamClick(enabled = options.tapEnabled),
        interactionConfig = options.interactionConfig,
        tooltip = streamTooltip(mode = options.tooltipMode),
        crosshair = streamCrosshair(enabled = options.crosshairEnabled),
    )
}

private fun streamingCode(
    chartType: StreamChartType,
    windowSize: Int,
    scrollback: Boolean,
    jumpOverlay: StreamJumpOverlay,
    tooltipMode: StreamTooltipMode,
    crosshairEnabled: Boolean,
    labelNewest: Boolean,
    animate: Boolean,
): String {
    val chartName =
        when (chartType) {
            StreamChartType.Line -> "LineChart"
            StreamChartType.Area -> "AreaChart"
            StreamChartType.Bar -> "BarChart"
            StreamChartType.HorizontalBar -> "HorizontalBarChart"
            StreamChartType.Point -> "PointChart"
        }
    val configName =
        when (chartType) {
            StreamChartType.Line, StreamChartType.Area -> "lineConfig = LineChartConfig"
            StreamChartType.Point -> "pointConfig = PointChartConfig"
            StreamChartType.Bar, StreamChartType.HorizontalBar -> "barConfig = BarChartConfig"
        }
    val clickName =
        when (chartType) {
            StreamChartType.Bar, StreamChartType.HorizontalBar -> "onBarClick"
            else -> "onPointClick"
        }
    val markerLine =
        if (labelNewest) {
            "\n                markers = listOf(PersistentMarker(dataIndex = -1)),  // pin a label to the latest value"
        } else {
            ""
        }
    val streamingLine =
        if (scrollback) {
            "\n                streamingState = streaming,  // drag the plot back through history"
        } else {
            ""
        }
    val jumpLine =
        when {
            !scrollback || jumpOverlay == StreamJumpOverlay.None -> ""
            jumpOverlay == StreamJumpOverlay.Pill ->
                "\n                jumpToLatest = { state -> ChartJumpToLatestPill(state = state) },"

            else ->
                "\n                jumpToLatest = { state -> CustomJumpToLatest(state = state) },  // any composable fits"
        }
    val interactionBlock =
        if (streamingLine.isEmpty() && jumpLine.isEmpty()) {
            "\n            interactionConfig = ChartInteractionConfig(),"
        } else {
            "\n            interactionConfig =\n                ChartInteractionConfig($streamingLine$jumpLine\n                ),"
        }
    val tooltipLine =
        when (tooltipMode) {
            StreamTooltipMode.Canvas -> "\n            tooltip = ChartTooltip.canvas(),"
            StreamTooltipMode.Compose -> "\n            tooltip = ChartTooltip.compose { StreamingBubble(text = text) },"
            StreamTooltipMode.None -> "\n            tooltip = ChartTooltip.none(),"
        }
    val clickLine =
        if (tooltipMode == StreamTooltipMode.None) {
            ""
        } else {
            "\n            $clickName = { },"
        }
    val crosshairLine =
        if (crosshairEnabled) {
            "\n            crosshair = ChartCrosshair(config = ChartCrosshairConfig(showHorizontalLine = false)),"
        } else {
            ""
        }
    return """
        val streaming = rememberStreamingState()

        $chartName(
            data = { streamData },              // append points over time
            color = ChartyColor.Solid(color),
            $configName(
                visibleWindow = $windowSize,    // show only the last $windowSize points
                animation = ${if (animate) "Animation.Fast" else "Animation.Disabled"},     // drives the horizontal slide$markerLine
            ),$interactionBlock$clickLine$tooltipLine$crosshairLine
        )
        """.trimIndent()
}
