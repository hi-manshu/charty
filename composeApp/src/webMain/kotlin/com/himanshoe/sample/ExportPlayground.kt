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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.export.ChartExportResult
import com.himanshoe.charty.export.capture
import com.himanshoe.charty.export.chartCapture
import com.himanshoe.charty.export.rememberChartCaptureController
import com.himanshoe.charty.export.rememberChartExporter
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import kotlinx.coroutines.launch

private val exportLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug")

/**
 * Demonstrates PNG export: the chart is wrapped in `Modifier.chartCapture`, and "Export PNG" reads the
 * recorded frame back with `capture()` before handing it to the platform exporter. In the browser that
 * means a download; on Android it writes to the app cache and on iOS it opens the share sheet.
 */
@Composable
internal fun ExportPlayground() {
    var pointCount by remember { mutableIntStateOf(8) }
    var smooth by remember { mutableStateOf(true) }
    var showPoints by remember { mutableStateOf(true) }
    var fileName by remember { mutableStateOf(ExportName.Revenue) }
    var color by remember { mutableStateOf(playgroundPalette[2]) }
    var status by remember { mutableStateOf("Not exported yet") }
    var tick by remember { mutableIntStateOf(0) }

    val controller = rememberChartCaptureController()
    val exporter = rememberChartExporter()
    val scope = rememberCoroutineScope()

    val data =
        remember(pointCount, tick) {
            randomValues(count = pointCount, tick = tick).mapIndexed { index, value ->
                LineData(label = exportLabels[index % exportLabels.size], value = value)
            }
        }

    val code =
        """
        // Wrap the chart once, then capture and export on demand.
        val controller = rememberChartCaptureController()
        val exporter = rememberChartExporter()

        LineChart(
            data = { revenueData },
            modifier = Modifier.fillMaxSize().chartCapture(controller),
            color = ChartyColor.Solid(Color(${colorHex(color)})),
            lineConfig = LineChartConfig(smoothCurve = $smooth, showPoints = $showPoints),
        )

        // ...from a coroutine, e.g. a button's onClick:
        val result = exporter.export(controller.capture(), "${fileName.value}")
        when (result) {
            is ChartExportResult.Saved -> show("Saved to " + result.location)
            ChartExportResult.Shared -> show("Shared")
            is ChartExportResult.Failed -> show("Failed: " + result.reason)
        }
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            LineChart(
                data = { data },
                modifier = Modifier.fillMaxSize().chartCapture(controller),
                scaffoldConfig = playgroundScaffoldConfig(),
                interactionConfig = playgroundInteractionConfig(pointCount = data.size),
                color = ChartyColor.Solid(color),
                lineConfig =
                    LineChartConfig(
                        smoothCurve = smooth,
                        showPoints = showPoints,
                        animation = playgroundAnimation(animate = LocalPlaygroundAnimate.current),
                    ),
            )
        },
        controls = {
            ControlSection(title = "Export")
            ChoiceRow(
                label = "File name",
                options = ExportName.entries.toList(),
                selected = fileName,
                labelOf = { it.value },
                onSelect = { fileName = it },
            )
            PlaygroundActionRow(
                primaryLabel = "Export PNG",
                onPrimary = {
                    status = "Exporting..."
                    scope.launch {
                        val result = exporter.export(bitmap = controller.capture(), fileName = fileName.value)
                        status =
                            when (result) {
                                is ChartExportResult.Saved -> "Saved: ${result.location}"
                                ChartExportResult.Shared -> "Shared via the system sheet"
                                is ChartExportResult.Failed -> "Failed: ${result.reason}"
                            }
                    }
                },
                secondaryLabel = "Reshuffle",
                onSecondary = { tick += 1 },
            )
            Text(text = status, style = MaterialTheme.typography.bodySmall)
            ControlSection(title = "Data")
            IntSliderRow(
                label = "Points",
                value = pointCount,
                valueRange = 4..24,
                onValueChange = { pointCount = it },
            )
            ControlSection(title = "Line")
            SwitchRow(label = "Smooth curve", checked = smooth, onCheckedChange = { smooth = it })
            SwitchRow(label = "Show points", checked = showPoints, onCheckedChange = { showPoints = it })
            ControlSection(title = "Color")
            ColorRow(label = "Line color", selected = color, onSelect = { color = it })
        },
    )
}

internal enum class ExportName(
    val value: String,
) {
    Revenue("revenue"),
    Quarterly("q1 report"),
    Blank(""),
}
