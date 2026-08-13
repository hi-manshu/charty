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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.heatmap.MatrixHeatmapChart
import com.himanshoe.charty.heatmap.config.MatrixHeatmapConfig
import com.himanshoe.charty.heatmap.data.HeatmapCell
import kotlin.math.abs
import kotlin.random.Random

private val WEEK_DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val HOURS = (8..19).map { it.toString() }
private const val PEAK_HOUR = 13
private const val WEEKDAY_BASE = 60f
private const val WEEKEND_BASE = 22f

internal enum class HeatmapScaleOption(
    val label: String,
    val stops: List<Color>?,
) {
    Blues(label = "Blues", stops = listOf(Color(0xFFE3F2FD), Color(0xFF0D47A1))),
    Sunset(label = "Sunset", stops = listOf(Color(0xFFFFF3E0), Color(0xFFFF9800), Color(0xFFBF360C))),
    Viridis(label = "Viridis", stops = listOf(Color(0xFF440154), Color(0xFF21918C), Color(0xFFFDE725))),
    Solid(label = "Solid hue", stops = null),
}

private fun demoCells(tick: Int): List<HeatmapCell> {
    val random = Random(seed = tick * 1000 + 7)
    return WEEK_DAYS.flatMapIndexed { dayIndex, day ->
        HOURS.map { hour ->
            val base = if (dayIndex >= 5) WEEKEND_BASE else WEEKDAY_BASE
            val peak = (1f - abs(hour.toInt() - PEAK_HOUR) / 8f).coerceAtLeast(0f)
            val value = base * (0.3f + 0.7f * peak) + random.nextFloat() * 15f
            HeatmapCell(rowLabel = day, columnLabel = hour, value = value)
        }
    }
}

/**
 * Interactive demo for [MatrixHeatmapChart]: a day-of-week × hour activity grid with a color-scale
 * chooser, a show-values toggle, spacing/corner sliders, a tap tooltip toggle, and a tap readout of
 * the clicked cell.
 */
@Composable
internal fun HeatmapPlayground() {
    var scaleOption by remember { mutableStateOf(HeatmapScaleOption.Blues) }
    var solidHue by remember { mutableStateOf(playgroundPalette[0]) }
    var showValues by remember { mutableStateOf(false) }
    var spacing by remember { mutableIntStateOf(2) }
    var cornerRadius by remember { mutableIntStateOf(4) }
    var tick by remember { mutableIntStateOf(0) }
    var clicked by remember { mutableStateOf<HeatmapCell?>(null) }
    var showTooltip by remember { mutableStateOf(true) }

    val cells = remember(tick) { demoCells(tick) }
    val colorScale =
        when (val stops = scaleOption.stops) {
            null -> ChartyColor.Solid(color = solidHue)
            else -> ChartyColor.Gradient(colors = stops)
        }
    val animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled

    val scaleCode =
        when (val stops = scaleOption.stops) {
            null -> "ChartyColor.Solid(Color(${colorHex(solidHue)}))"
            else -> "ChartyColor.Gradient(listOf(${stops.joinToString { "Color(${colorHex(it)})" }}))"
        }
    val code =
        """
        // Day-of-week × hour activity heatmap.
        MatrixHeatmapChart(
            data = { cells },                   // List<HeatmapCell>(rowLabel, columnLabel, value)
            config = MatrixHeatmapConfig(
                colorScale = $scaleCode,
                cellSpacing = $spacing.dp,
                cellCornerRadius = ${fc(cornerRadius.toFloat())},
                showValues = $showValues,
                tooltipFormatter = { cell -> "${'$'}{cell.rowLabel} ${'$'}{cell.columnLabel}:00 → ${'$'}{cell.value.toInt()}" },
            ),
            onCellClick = { cell -> /* ${clicked?.let { "${it.rowLabel} ${it.columnLabel}h" } ?: "tap a cell"} */ },
            tooltip = ${if (showTooltip) "ChartTooltip.canvas()" else "ChartTooltip.none()"},
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            MatrixHeatmapChart(
                data = { cells },
                modifier = Modifier.fillMaxSize(),
                config =
                    MatrixHeatmapConfig(
                        colorScale = colorScale,
                        cellSpacing = spacing.dp,
                        cellCornerRadius = cornerRadius.toFloat(),
                        showValues = showValues,
                        tooltipFormatter = { cell ->
                            "${cell.rowLabel} ${cell.columnLabel}:00 → ${cell.value.toInt()}"
                        },
                        animation = animation,
                    ),
                onCellClick = { clicked = it },
                tooltip = if (showTooltip) ChartTooltip.canvas() else ChartTooltip.none(),
            )
        },
        controls = {
            ControlSection(title = "Color scale")
            ChoiceRow(
                label = "Scale",
                options = HeatmapScaleOption.entries.toList(),
                selected = scaleOption,
                labelOf = { it.label },
                onSelect = { scaleOption = it },
            )
            if (scaleOption == HeatmapScaleOption.Solid) {
                ColorRow(label = "Hue", selected = solidHue, onSelect = { solidHue = it })
            }
            ControlSection(title = "Cells")
            SwitchRow(label = "Show values", checked = showValues, onCheckedChange = { showValues = it })
            IntSliderRow(
                label = "Cell spacing (dp)",
                value = spacing,
                valueRange = 0..8,
                onValueChange = { spacing = it },
            )
            IntSliderRow(
                label = "Corner radius (px)",
                value = cornerRadius,
                valueRange = 0..12,
                onValueChange = { cornerRadius = it },
            )
            ControlSection(title = "Tooltip")
            SwitchRow(
                label = "Tap tooltip",
                checked = showTooltip,
                onCheckedChange = { showTooltip = it },
            )
            ControlSection(title = "Data")
            PlaygroundActionRow(primaryLabel = "Shuffle data", onPrimary = { tick++ })
            ControlSection(title = "Last click")
            Text(
                text =
                    clicked?.let { cell ->
                        "${cell.rowLabel} at ${cell.columnLabel}:00 → ${cell.value.toInt()}"
                    } ?: "Tap a cell with data",
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}
