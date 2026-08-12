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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.rememberCrosshairSync
import com.himanshoe.charty.common.gesture.rememberParticipant
import com.himanshoe.charty.common.viewport.rememberViewPortState
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import kotlin.random.Random

private const val SYNC_VALUE_MIN = 20f
private const val SYNC_VALUE_MAX = 90f

private fun relatedSeries(
    count: Int,
    seed: Int,
): Pair<List<Float>, List<Float>> {
    val random = Random(seed)
    val revenue = List(count) { SYNC_VALUE_MIN + random.nextFloat() * (SYNC_VALUE_MAX - SYNC_VALUE_MIN) }
    val orders =
        revenue.map { value ->
            (value * 0.6f + random.nextFloat() * 18f).coerceIn(SYNC_VALUE_MIN, SYNC_VALUE_MAX)
        }
    return revenue to orders
}

/**
 * Synchronized-crosshair demo: two stacked [LineChart]s over related series share one
 * `CrosshairSyncState`, so dragging on either chart is meant to show the guide line on both. The
 * full cross-chart mirroring waits on a small library hook (see the code panel); until it lands,
 * each chart's crosshair works normally and this screen demonstrates the sync API surface.
 */
@Composable
internal fun SyncedPlayground() {
    var pointCount by remember { mutableStateOf(12) }
    var seed by remember { mutableStateOf(1) }
    var topColor by remember { mutableStateOf(playgroundPalette[0]) }
    var bottomColor by remember { mutableStateOf(playgroundPalette[2]) }
    var pinOnRelease by remember { mutableStateOf(false) }
    var smooth by remember { mutableStateOf(true) }

    val (topValues, bottomValues) =
        remember(pointCount, seed) { relatedSeries(count = pointCount, seed = seed) }
    val topData = topValues.mapIndexed { i, v -> LineData(label = "D${i + 1}", value = v) }
    val bottomData = bottomValues.mapIndexed { i, v -> LineData(label = "D${i + 1}", value = v) }

    val code =
        """
        // One CrosshairSyncState shared by both stacked charts.
        val sync = rememberCrosshairSync()

        val revenueViewport = rememberViewPortState()
        LineChart(
            data = { revenue },
            crosshair = sync.rememberParticipant(
                ownerId = "revenue",
                viewPortState = revenueViewport,
                config = ChartCrosshairConfig(dismissOnRelease = ${!pinOnRelease}),
            ),
            interactionConfig = ChartInteractionConfig(viewPortState = revenueViewport),
        )

        val ordersViewport = rememberViewPortState()
        LineChart(
            data = { orders },
            crosshair = sync.rememberParticipant(
                ownerId = "orders",
                viewPortState = ordersViewport,
                config = ChartCrosshairConfig(dismissOnRelease = ${!pinOnRelease}),
            ),
            interactionConfig = ChartInteractionConfig(viewPortState = ordersViewport),
        )

        // Pending library hook for full cross-chart mirroring:
        //   1) ChartCrosshair gains `val manager: CrosshairManager<T>? = null`
        //   2) rememberChartCrosshair(enabled, external = crosshair?.manager) adopts it
        // Until charts adopt the participant's manager, each guide stays chart-local.
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            SyncedCharts(
                topData = topData,
                bottomData = bottomData,
                topColor = topColor,
                bottomColor = bottomColor,
                pinOnRelease = pinOnRelease,
                smooth = smooth,
            )
        },
        controls = {
            ControlSection(title = "Data")
            IntSliderRow(
                label = "Points per series",
                value = pointCount,
                valueRange = 6..24,
                onValueChange = { pointCount = it },
            )
            PlaygroundActionRow(
                primaryLabel = "Shuffle data",
                onPrimary = { seed += 1 },
            )
            ControlSection(title = "Crosshair")
            SwitchRow(label = "Pin on release", checked = pinOnRelease, onCheckedChange = { pinOnRelease = it })
            SwitchRow(label = "Smooth curve", checked = smooth, onCheckedChange = { smooth = it })
            ControlSection(title = "Colors")
            ColorRow(label = "Revenue (top)", selected = topColor, onSelect = { topColor = it })
            ColorRow(label = "Orders (bottom)", selected = bottomColor, onSelect = { bottomColor = it })
            ControlSection(title = "Status")
            Text(
                text = "Drag across a chart to scrub its crosshair. Cross-chart mirroring activates once the ChartCrosshair.manager hook lands in the library.",
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}

@Composable
private fun SyncedCharts(
    topData: List<LineData>,
    bottomData: List<LineData>,
    topColor: Color,
    bottomColor: Color,
    pinOnRelease: Boolean,
    smooth: Boolean,
) {
    val sync = rememberCrosshairSync()
    val crosshairConfig = ChartCrosshairConfig(showHorizontalLine = false, dismissOnRelease = !pinOnRelease)
    Column(modifier = Modifier.fillMaxSize()) {
        val topViewport = rememberViewPortState()
        LineChart(
            data = { topData },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = ChartyColor.Solid(topColor),
            lineConfig = LineChartConfig(smoothCurve = smooth, animation = Animation.Disabled),
            crosshair =
                sync.rememberParticipant<LineData>(
                    ownerId = "revenue",
                    viewPortState = topViewport,
                    config = crosshairConfig,
                ),
            interactionConfig = ChartInteractionConfig(viewPortState = topViewport),
        )
        Spacer(modifier = Modifier.height(12.dp))
        val bottomViewport = rememberViewPortState()
        LineChart(
            data = { bottomData },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = ChartyColor.Solid(bottomColor),
            lineConfig = LineChartConfig(smoothCurve = smooth, animation = Animation.Disabled),
            crosshair =
                sync.rememberParticipant<LineData>(
                    ownerId = "orders",
                    viewPortState = bottomViewport,
                    config = crosshairConfig,
                ),
            interactionConfig = ChartInteractionConfig(viewPortState = bottomViewport),
        )
    }
}
