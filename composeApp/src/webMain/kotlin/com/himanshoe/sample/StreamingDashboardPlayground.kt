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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.CrosshairSyncScope
import com.himanshoe.charty.common.streaming.ChartJumpToLatestPill
import com.himanshoe.charty.common.streaming.rememberStreamingState
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.point.PointChart
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.PointData
import kotlin.random.Random
import kotlinx.coroutines.delay

private const val DASHBOARD_INTERVAL_MILLIS = 800L
private const val DASHBOARD_SEED_COUNT = 14

/** One tick of the shared feed: three correlated metrics sampled at the same instant. */
@Immutable
private class Reading(
    val throughput: Float,
    val requests: Float,
    val latency: Float,
)

private fun nextReading(): Reading {
    val throughput = 30f + Random.nextFloat() * 60f
    return Reading(
        throughput = throughput,
        requests = (throughput * 0.7f + Random.nextFloat() * 20f),
        latency = (110f - throughput * 0.6f + Random.nextFloat() * 25f),
    )
}

/**
 * A realistic streaming dashboard: one appending feed drives three charts at once, each with the same
 * rolling `visibleWindow` and its own [com.himanshoe.charty.common.streaming.StreamingState], so every
 * panel can be scrolled back through history independently and jumped back to the live edge.
 *
 * The whole stack sits inside a [CrosshairSyncScope] with a crosshair on each chart, which is how a
 * group of charts is wired to share one guide position.
 */
@Composable
internal fun StreamingDashboardPlayground() {
    var windowSize by remember { mutableIntStateOf(10) }
    var auto by remember { mutableStateOf(true) }
    var labelNewest by remember { mutableStateOf(true) }
    var crosshairEnabled by remember { mutableStateOf(true) }
    var throughputColor by remember { mutableStateOf(playgroundPalette[0]) }
    var requestsColor by remember { mutableStateOf(playgroundPalette[3]) }
    var latencyColor by remember { mutableStateOf(playgroundPalette[4]) }

    val feed = remember { mutableStateListOf<Reading>().apply { repeat(DASHBOARD_SEED_COUNT) { add(nextReading()) } } }

    LaunchedEffect(auto) {
        while (auto) {
            delay(DASHBOARD_INTERVAL_MILLIS)
            feed.add(nextReading())
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
    val crosshairConfig = ChartCrosshairConfig(showHorizontalLine = false, dismissOnRelease = true)

    val code =
        """
        val feed = remember { mutableStateListOf<Reading>() }   // one appending source

        CrosshairSyncScope {
            val throughput = rememberStreamingState()
            LineChart(
                data = { feed.map { LineData(it.tick, it.throughput) } },
                lineConfig = LineChartConfig(visibleWindow = $windowSize, animation = ${if (LocalPlaygroundAnimate.current) "Animation.Fast" else "Animation.Disabled"}),
                interactionConfig = ChartInteractionConfig(
                    streamingState = throughput,
                    jumpToLatest = { state -> ChartJumpToLatestPill(state = state) },
                ),
                crosshair = ChartCrosshair(config = ChartCrosshairConfig(showHorizontalLine = false)),
            )

            val requests = rememberStreamingState()
            BarChart(data = { … }, barConfig = BarChartConfig(visibleWindow = $windowSize), …)

            val latency = rememberStreamingState()
            PointChart(data = { … }, pointConfig = PointChartConfig(visibleWindow = $windowSize), …)
        }
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            StreamingDashboardCharts(
                feed = feed,
                windowSize = windowSize,
                animation = animation,
                markers = markers,
                crosshairConfig =
                    if (crosshairEnabled) {
                        crosshairConfig
                    } else {
                        null
                    },
                throughputColor = ChartyColor.Solid(throughputColor),
                requestsColor = ChartyColor.Solid(requestsColor),
                latencyColor = ChartyColor.Solid(latencyColor),
            )
        },
        controls = {
            ControlSection(title = "Feed")
            SwitchRow(label = "Auto (add every 0.8s)", checked = auto, onCheckedChange = { auto = it })
            SwitchRow(label = "Label newest point", checked = labelNewest, onCheckedChange = { labelNewest = it })
            PlaygroundActionRow(
                primaryLabel = "Add reading",
                onPrimary = { feed.add(nextReading()) },
                secondaryLabel = "Clear",
                onSecondary = {
                    feed.clear()
                    repeat(DASHBOARD_SEED_COUNT) { feed.add(nextReading()) }
                },
            )
            ControlSection(title = "Window")
            IntSliderRow(
                label = "Visible points",
                value = windowSize,
                valueRange = 4..20,
                onValueChange = { windowSize = it },
            )
            ControlSection(title = "Crosshair")
            SwitchRow(
                label = "Synced crosshair",
                checked = crosshairEnabled,
                onCheckedChange = { crosshairEnabled = it },
            )
            Text(
                text = "Every panel is inside one CrosshairSyncScope, so the group shares a single guide position; each panel keeps its own StreamingState for scrollback.",
                style = MaterialTheme.typography.bodySmall,
            )
            ControlSection(title = "Live")
            Text(text = "readings: ${feed.size}", style = MaterialTheme.typography.bodySmall)
            ControlSection(title = "Colors")
            ColorRow(label = "Throughput", selected = throughputColor, onSelect = { throughputColor = it })
            ColorRow(label = "Requests", selected = requestsColor, onSelect = { requestsColor = it })
            ColorRow(label = "Latency", selected = latencyColor, onSelect = { latencyColor = it })
        },
    )
}

@Composable
private fun StreamingDashboardCharts(
    feed: List<Reading>,
    windowSize: Int,
    animation: Animation,
    markers: List<PersistentMarker>,
    crosshairConfig: ChartCrosshairConfig?,
    throughputColor: ChartyColor,
    requestsColor: ChartyColor,
    latencyColor: ChartyColor,
) {
    val throughput = feed.mapIndexed { index, r -> LineData(label = (index + 1).toString(), value = r.throughput) }
    val requests = feed.mapIndexed { index, r -> BarData(label = (index + 1).toString(), value = r.requests) }
    val latency = feed.mapIndexed { index, r -> PointData(label = (index + 1).toString(), value = r.latency) }

    CrosshairSyncScope {
        Column(modifier = Modifier.fillMaxSize()) {
            val throughputStream = rememberStreamingState()
            LineChart(
                data = { throughput },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = throughputColor,
                lineConfig =
                    LineChartConfig(
                        visibleWindow = windowSize,
                        animation = animation,
                        markers = markers,
                    ),
                scaffoldConfig = playgroundScaffoldConfig(),
                interactionConfig =
                    withPlaygroundInteractions(
                        base =
                            ChartInteractionConfig(
                                streamingState = throughputStream,
                                jumpToLatest = { state -> ChartJumpToLatestPill(state = state) },
                            ),
                    ),
                crosshair = crosshairConfig?.let { ChartCrosshair(config = it) },
                tooltip = ChartTooltip.none(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            val requestsStream = rememberStreamingState()
            BarChart(
                data = { requests },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = requestsColor,
                barConfig =
                    BarChartConfig(
                        visibleWindow = windowSize,
                        animation = animation,
                        markers = markers,
                    ),
                scaffoldConfig = playgroundScaffoldConfig(),
                interactionConfig =
                    withPlaygroundInteractions(
                        base =
                            ChartInteractionConfig(
                                streamingState = requestsStream,
                                jumpToLatest = { state -> ChartJumpToLatestPill(state = state) },
                            ),
                    ),
                tooltip = ChartTooltip.none(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            val latencyStream = rememberStreamingState()
            PointChart(
                data = { latency },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = latencyColor,
                pointConfig =
                    PointChartConfig(
                        visibleWindow = windowSize,
                        animation = animation,
                        markers = markers,
                    ),
                scaffoldConfig = playgroundScaffoldConfig(),
                interactionConfig =
                    withPlaygroundInteractions(
                        base =
                            ChartInteractionConfig(
                                streamingState = latencyStream,
                                jumpToLatest = { state -> ChartJumpToLatestPill(state = state) },
                            ),
                    ),
                crosshair = crosshairConfig?.let { ChartCrosshair(config = it) },
                tooltip = ChartTooltip.none(),
            )
        }
    }
}
