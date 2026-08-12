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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import kotlin.random.Random
import kotlinx.coroutines.delay

private const val AUTO_INTERVAL_MILLIS = 700L
private const val SEED_COUNT = 12
private const val VALUE_MIN = 15f
private const val VALUE_MAX = 95f

private fun nextValue(): Float = VALUE_MIN + Random.nextFloat() * (VALUE_MAX - VALUE_MIN)

/**
 * Live streaming demo driving the real [LineChart] with a rolling `visibleWindow`. The chart owns the
 * slide: appending to the data list advances the window and the production animation eases the last
 * `windowSize` points — and their x-axis labels — leftwards while the new point enters at the right.
 * There is no hand-rolled animation here; this is exactly the public API a consumer would use.
 */
@Composable
internal fun StreamingLinePlayground() {
    var windowSize by remember { mutableStateOf(8) }
    var auto by remember { mutableStateOf(true) }
    var color by remember { mutableStateOf(playgroundPalette[0]) }

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

    val chartData = values.mapIndexed { index, value -> LineData(label = (index + 1).toString(), value = value) }

    val code =
        """
        // The real LineChart with a rolling window — the chart owns the slide.
        LineChart(
            data = { streamData },              // append points over time
            color = ChartyColor.Solid(color),
            lineConfig = LineChartConfig(
                visibleWindow = $windowSize,    // show only the last $windowSize points
                animation = Animation.Fast,     // drives the slide easing
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            LineChart(
                data = { chartData },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color),
                lineConfig =
                    LineChartConfig(
                        visibleWindow = windowSize,
                        animation = Animation.Fast,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Stream")
            SwitchRow(label = "Auto (add every 0.7s)", checked = auto, onCheckedChange = { auto = it })
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
            ControlSection(title = "Live")
            Text(
                text = "total points: ${values.size}",
                style = MaterialTheme.typography.bodySmall,
            )
            ControlSection(title = "Color")
            ColorRow(label = "Line color", selected = color, onSelect = { color = it })
        },
    )
}
